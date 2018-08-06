/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.returntomapframe.localization.localize;

import android.support.annotation.NonNull;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

class LocalizeMachine {

    @NonNull
    private final BehaviorSubject<LocalizeState> subject = BehaviorSubject.createDefault(LocalizeState.IDLE);

    void post(@NonNull LocalizeEvent event) {
        LocalizeState currentState = subject.getValue();
        if (currentState == null) {
            throw new IllegalStateException("LocalizeMachine must have a LocalizeState to be able to handle a LocalizeEvent.");
        }

        LocalizeState newState = reduce(currentState, event);
        subject.onNext(newState);
    }

    @NonNull
    Observable<LocalizeState> localizeState() {
        return subject.distinctUntilChanged();
    }

    @NonNull
    private LocalizeState reduce(@NonNull LocalizeState currentState, @NonNull LocalizeEvent event) {
        switch (event) {
            case START:
                if (currentState.equals(LocalizeState.IDLE)) {
                    return LocalizeState.BRIEFING;
                }
                break;
            case STOP:
                return LocalizeState.IDLE;
            case START_LOCALIZE:
                if (currentState.equals(LocalizeState.BRIEFING) || currentState.equals(LocalizeState.ERROR)) {
                    return LocalizeState.LOCALIZING;
                }
                break;
            case LOCALIZE_SUCCEEDED:
                if (currentState.equals(LocalizeState.LOCALIZING)) {
                    return LocalizeState.SUCCESS;
                }
                break;
            case LOCALIZE_FAILED:
                if (currentState.equals(LocalizeState.LOCALIZING)) {
                    return LocalizeState.ERROR;
                }
                break;
            case SUCCESS_CONFIRMED:
                if (currentState.equals(LocalizeState.SUCCESS)) {
                    return LocalizeState.END;
                }
                break;
        }

        return currentState;
    }
}
