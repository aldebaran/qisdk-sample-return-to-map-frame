/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.returntomapframe.mapping;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.util.Log;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.LocalizeAndMapBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.object.actuation.LocalizationStatus;
import com.aldebaran.qi.sdk.object.actuation.LocalizeAndMap;
import com.softbankrobotics.sample.returntomapframe.R;
import com.softbankrobotics.sample.returntomapframe.core.MapManager;
import com.softbankrobotics.sample.returntomapframe.utils.FutureCancellations;

import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

class MappingRobot implements RobotLifecycleCallbacks {

    private static final String TAG = "MappingRobot";

    @NonNull
    private final MappingMachine machine;

    @Nullable
    private Disposable disposable;

    @Nullable
    private QiContext qiContext;
    @Nullable
    private LocalizeAndMap localizeAndMap;
    @Nullable
    private Future<Void> mapping;
    @Nullable
    private Future<Void> speech;

    MappingRobot(@NonNull MappingMachine machine) {
        this.machine = machine;
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        this.qiContext = qiContext;

        machine.post(MappingEvent.FOCUS_GAINED);

        disposable = machine.mappingState()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(this::onMappingStateChanged);
    }

    @Override
    public void onRobotFocusLost() {
        machine.post(MappingEvent.FOCUS_LOST);

        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }

        this.qiContext = null;
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        Log.e(TAG, "onRobotFocusRefused: " + reason);
    }

    private void startMapping() {
        if (qiContext == null) {
            Log.e(TAG, "Error while mapping: qiContext is null");
            machine.post(MappingEvent.MAPPING_FAILED);
            return;
        }

        mapping = LocalizeAndMapBuilder.with(qiContext).buildAsync()
                .andThenCompose(loc -> {
                    localizeAndMap = loc;

                    localizeAndMap.addOnStatusChangedListener(status -> {
                        if (status == LocalizationStatus.LOCALIZED) {
                            stopMapping();
                            machine.post(MappingEvent.MAPPING_SUCCEEDED);
                        }
                    });

                    return localizeAndMap.async().run();
                })
                .thenConsume(future -> {
                    if (localizeAndMap != null) {
                        localizeAndMap.removeAllOnStatusChangedListeners();
                    }

                    if (future.hasError()) {
                        Log.e(TAG, "Error while mapping", future.getError());
                        machine.post(MappingEvent.MAPPING_FAILED);
                    }
                });
    }

    private void stopMapping() {
        if (mapping != null) {
            mapping.cancel(true);
        }
    }

    private void saveMap() {
        if (localizeAndMap == null) {
            Log.e(TAG, "Error while saving map: localizeAndMap is null");
            machine.post(MappingEvent.SAVING_MAP_FAILED);
            return;
        }

        localizeAndMap.async().dumpMap()
                .andThenCompose(map -> {
                    if (qiContext == null) {
                        throw new IllegalStateException("qiContext is null");
                    }

                    Log.d(TAG, "Saving map...");
                    return MapManager.getInstance().saveMap(qiContext, map);
                })
                .thenConsume(future -> {
                    if (future.isSuccess()) {
                        Log.d(TAG, "Map saved successfully");
                        machine.post(MappingEvent.SAVING_MAP_SUCCEEDED);
                    } else if (future.hasError()) {
                        Log.e(TAG, "Error while saving map", future.getError());
                        machine.post(MappingEvent.SAVING_MAP_FAILED);
                    }
                });
    }

    @NonNull
    private Future<Void> say(@StringRes int resId) {
        return FutureCancellations.cancel(speech)
                .andThenCompose(ignored -> {
                    if (qiContext == null) {
                        throw new IllegalStateException("qiContext is null");
                    }

                    Future<Void> newSpeech = SayBuilder.with(qiContext)
                            .withText(qiContext.getString(resId))
                            .buildAsync()
                            .andThenCompose(say -> say.async().run());

                    speech = newSpeech;
                    return newSpeech;
                });
    }

    @NonNull
    private Future<Void> cancelCurrentActions() {
        return FutureCancellations.cancelAll(speech, mapping);
    }

    private void onMappingStateChanged(@NonNull MappingState mappingState) {
        Log.d(TAG, "onMappingStateChanged: " + mappingState);

        switch (mappingState) {
            case IDLE:
            case END:
                cancelCurrentActions();
                break;
            case BRIEFING:
                cancelCurrentActions()
                        .andThenCompose(ignored -> say(R.string.briefing_speech));
                break;
            case ADVICES:
                cancelCurrentActions()
                        .andThenCompose(ignored -> say(R.string.mapping_mapping_speech))
                        .andThenCompose(ignored -> say(R.string.countdown_speech))
                        .andThenConsume(ignored -> machine.post(MappingEvent.ADVICES_ENDED));
                break;
            case MAPPING:
                cancelCurrentActions()
                        .andThenConsume(ignored -> startMapping());
                break;
            case SAVING_MAP:
                cancelCurrentActions()
                        .andThenCompose(ignored -> say(R.string.mapping_saving_map_speech))
                        .andThenConsume(ignored -> saveMap());
                break;
            case ERROR:
                cancelCurrentActions()
                        .andThenCompose(ignored -> say(R.string.error_speech));
                break;
            case SUCCESS:
                cancelCurrentActions()
                        .andThenCompose(ignored -> say(R.string.mapping_success_speech))
                        .andThenConsume(ignored -> machine.post(MappingEvent.SUCCESS_CONFIRMED));
                break;
        }
    }
}
