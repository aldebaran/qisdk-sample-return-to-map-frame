/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.returntomapframe.mapping;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.LocalizeAndMapBuilder;
import com.aldebaran.qi.sdk.object.actuation.LocalizationStatus;
import com.aldebaran.qi.sdk.object.actuation.LocalizeAndMap;
import com.softbankrobotics.sample.returntomapframe.core.MapManager;

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
                            saveMap();
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
            machine.post(MappingEvent.MAPPING_FAILED);
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
                        machine.post(MappingEvent.MAPPING_SUCCEEDED);
                    } else if (future.hasError()) {
                        Log.e(TAG, "Error while saving map", future.getError());
                        machine.post(MappingEvent.MAPPING_FAILED);
                    }
                });
    }

    private void onMappingStateChanged(@NonNull MappingState mappingState) {
        Log.d(TAG, "onMappingStateChanged: " + mappingState);

        switch (mappingState) {
            case IDLE:
                break;
            case BRIEFING:
                break;
            case MAPPING:
                break;
            case ERROR:
                break;
            case SUCCESS:
                break;
            case END:
                break;
        }
    }
}
