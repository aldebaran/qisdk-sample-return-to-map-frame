/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.returntomapframe.localization.localize;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.util.Log;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.softbankrobotics.sample.returntomapframe.R;
import com.softbankrobotics.sample.returntomapframe.localization.LocalizeManager;
import com.softbankrobotics.sample.returntomapframe.localization.Robot;
import com.softbankrobotics.sample.returntomapframe.utils.FutureCancellations;

import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * The robot for {@link LocalizeScreen}.
 */
class LocalizeRobot implements Robot {

    private static final String TAG = "LocalizeRobot";

    @NonNull
    private final LocalizeMachine machine;
    @NonNull
    private final LocalizeManager localizeManager;

    @Nullable
    private QiContext qiContext;

    @Nullable
    private Disposable disposable;

    @Nullable
    private Future<Void> speech;

    LocalizeRobot(@NonNull LocalizeMachine machine, @NonNull LocalizeManager localizeManager) {
        this.machine = machine;
        this.localizeManager = localizeManager;
    }

    @NonNull
    @Override
    public Future<Void> stop() {
        machine.post(LocalizeEvent.STOP);

        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }

        this.qiContext = null;

        return FutureCancellations.cancel(speech);
    }

    void start(@NonNull QiContext qiContext) {
        this.qiContext = qiContext;
        machine.post(LocalizeEvent.START);

        disposable = machine.localizeState()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(this::onLocalizeStateChanged);
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

    private void onLocalizeStateChanged(@NonNull LocalizeState localizeState) {
        Log.d(TAG, "onLocalizeStateChanged: " + localizeState);

        switch (localizeState) {
            case IDLE:
            case END:
                FutureCancellations.cancel(speech);
                break;
            case BRIEFING:
                say(R.string.briefing_speech);
                break;
            case ADVICES:
                say(R.string.localize_advices_speech)
                        .andThenCompose(ignored -> say(R.string.countdown_speech))
                        .andThenConsume(ignored -> machine.post(LocalizeEvent.ADVICES_ENDED));
                break;
            case LOADING_MAP:
                if (qiContext == null) {
                    machine.post(LocalizeEvent.LOADING_MAP_FAILED);
                    return;
                }

                localizeManager.loadMap(qiContext)
                        .thenConsume(future -> {
                            if (future.isSuccess()) {
                                machine.post(LocalizeEvent.LOADING_MAP_SUCCEEDED);
                            } else if (future.hasError()) {
                                machine.post(LocalizeEvent.LOADING_MAP_FAILED);
                            }
                        });
                break;
            case LOCALIZING:
                if (qiContext == null) {
                    machine.post(LocalizeEvent.LOCALIZE_FAILED);
                    return;
                }

                localizeManager.startLocalizing()
                        .thenConsume(future -> {
                            if (future.isSuccess()) {
                                machine.post(LocalizeEvent.LOCALIZE_SUCCEEDED);
                            } else if (future.hasError()) {
                                machine.post(LocalizeEvent.LOCALIZE_FAILED);
                            }
                        });
                break;
            case ERROR:
                say(R.string.error_speech);
                break;
            case SUCCESS:
                say(R.string.localize_success_speech)
                        .andThenConsume(ignored -> machine.post(LocalizeEvent.SUCCESS_CONFIRMED));
                break;
        }
    }
}
