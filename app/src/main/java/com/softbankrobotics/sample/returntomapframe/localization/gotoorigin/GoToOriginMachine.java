/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.returntomapframe.localization.gotoorigin;

import android.support.annotation.NonNull;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

/**
 * The state machine for {@link GoToOriginScreen}.
 */
class GoToOriginMachine {

    @NonNull
    private final BehaviorSubject<GoToOriginState> subject = BehaviorSubject.createDefault(GoToOriginState.IDLE);

    /**
     * Post an event to the machine.
     *
     * @param event the event
     */
    void post(@NonNull GoToOriginEvent event) {
        GoToOriginState currentState = subject.getValue();
        if (currentState == null) {
            throw new IllegalStateException("GoToOriginMachine must have a GoToOriginState to be able to handle a GoToOriginEvent.");
        }

        GoToOriginState newState = reduce(currentState, event);
        subject.onNext(newState);
    }

    /**
     * Provide the current {@link GoToOriginState}.
     *
     * @return The current {@link GoToOriginState}.
     */
    @NonNull
    Observable<GoToOriginState> goToOriginState() {
        return subject.distinctUntilChanged();
    }

    @NonNull
    private GoToOriginState reduce(@NonNull GoToOriginState currentState, @NonNull GoToOriginEvent event) {
        switch (event) {
            case START:
                if (currentState.equals(GoToOriginState.IDLE)) {
                    return GoToOriginState.BRIEFING;
                }
                break;
            case STOP:
                return GoToOriginState.IDLE;
            case START_GO_TO_ORIGIN:
                if (currentState.equals(GoToOriginState.BRIEFING) || currentState.equals(GoToOriginState.ERROR)) {
                    return GoToOriginState.MOVING;
                }
                break;
            case GO_TO_ORIGIN_SUCCEEDED:
                if (currentState.equals(GoToOriginState.MOVING)) {
                    return GoToOriginState.SUCCESS;
                }
                break;
            case GO_TO_ORIGIN_FAILED:
                if (currentState.equals(GoToOriginState.MOVING)) {
                    return GoToOriginState.ERROR;
                }
                break;
            case SUCCESS_CONFIRMED:
                if (currentState.equals(GoToOriginState.SUCCESS)) {
                    return GoToOriginState.END;
                }
                break;
        }

        return currentState;
    }
}
