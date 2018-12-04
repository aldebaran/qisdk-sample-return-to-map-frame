/*
 * Copyright (C) 2018 SoftBank Robotics Europe
 * See COPYING for the licence
 */
package com.softbankrobotics.sample.returntomapframe.localization.gotoorigin;

import android.util.Log;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.GoToBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.softbankrobotics.sample.returntomapframe.R;
import com.softbankrobotics.sample.returntomapframe.localization.Robot;
import com.softbankrobotics.sample.returntomapframe.utils.FutureCancellations;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * The robot for {@link GoToOriginScreen}.
 */
class GoToOriginRobot implements Robot {

    @NonNull
    private static final String TAG = "GoToOriginRobot";

    @NonNull
    private final GoToOriginMachine machine;

    @Nullable
    private QiContext qiContext;

    @Nullable
    private Disposable disposable;

    @Nullable
    private Future<Void> speech;
    @Nullable
    private Future<Void> movement;

    GoToOriginRobot(@NonNull GoToOriginMachine machine) {
        this.machine = machine;
    }

    @NonNull
    @Override
    public Future<Void> stop() {
        machine.post(GoToOriginEvent.STOP);

        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }

        this.qiContext = null;

        return FutureCancellations.cancel(speech);
    }

    public void start(@NonNull QiContext qiContext) {
        this.qiContext = qiContext;
        machine.post(GoToOriginEvent.START);

        disposable = machine.goToOriginState()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(this::onGoToOriginStateChanged);
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

    private void goToMapFrame() {
        if (qiContext == null) {
            Log.e(TAG, "Error while going to map frame: qiContext is null");
            machine.post(GoToOriginEvent.GO_TO_ORIGIN_FAILED);
            return;
        }

        // Retrieve the map frame and go to it.
        movement = qiContext.getMapping().async().mapFrame()
                .andThenCompose(mapFrame -> GoToBuilder.with(qiContext).withFrame(mapFrame).buildAsync())
                .andThenCompose(goTo -> goTo.async().run())
                .thenConsume(future -> {
                    if (future.isSuccess()) {
                        Log.d(TAG, "Map frame reached successfully");
                        machine.post(GoToOriginEvent.GO_TO_ORIGIN_SUCCEEDED);
                    } else if (future.hasError()) {
                        Log.e(TAG, "Error while going to map frame", future.getError());
                        machine.post(GoToOriginEvent.GO_TO_ORIGIN_FAILED);
                    }
                });
    }

    @NonNull
    private Future<Void> cancelCurrentActions() {
        return FutureCancellations.cancel(speech, movement);
    }

    private void onGoToOriginStateChanged(@NonNull GoToOriginState goToOriginState) {
        Log.d(TAG, "onGoToOriginStateChanged: " + goToOriginState);

        switch (goToOriginState) {
            case IDLE:
            case END:
                cancelCurrentActions();
                break;
            case BRIEFING:
                cancelCurrentActions()
                        .andThenCompose(ignored -> say(R.string.go_to_origin_briefing_speech));
                break;
            case MOVING:
                cancelCurrentActions()
                        .andThenConsume(ignored -> goToMapFrame());
                break;
            case ERROR:
                cancelCurrentActions()
                        .andThenCompose(ignored -> say(R.string.go_to_origin_error_speech));
                break;
            case SUCCESS:
                cancelCurrentActions()
                        .andThenCompose(ignored -> say(R.string.go_to_origin_success_speech))
                        .andThenConsume(ignored -> machine.post(GoToOriginEvent.SUCCESS_CONFIRMED));
                break;
        }
    }
}
