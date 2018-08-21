/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.returntomapframe.mapping;

import android.support.annotation.NonNull;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

/**
 * The state machine for {@link MappingActivity}.
 */
class MappingMachine {

    @NonNull
    private final BehaviorSubject<MappingState> subject = BehaviorSubject.createDefault(MappingState.IDLE);

    /**
     * Post an event to the machine.
     *
     * @param event the event
     */
    void post(@NonNull MappingEvent event) {
        MappingState currentState = subject.getValue();
        if (currentState == null) {
            throw new IllegalStateException("MappingMachine must have a MappingState to be able to handle a MappingEvent.");
        }

        MappingState newState = reduce(currentState, event);
        subject.onNext(newState);
    }

    /**
     * Provide the current {@link MappingState}.
     *
     * @return The current {@link MappingState}.
     */
    @NonNull
    Observable<MappingState> mappingState() {
        return subject.distinctUntilChanged();
    }

    @NonNull
    private MappingState reduce(@NonNull MappingState currentState, @NonNull MappingEvent event) {
        switch (event) {
            case FOCUS_GAINED:
                if (currentState.equals(MappingState.IDLE)) {
                    return MappingState.BRIEFING;
                }
                break;
            case FOCUS_LOST:
                return MappingState.IDLE;
            case START_MAPPING:
                if (currentState.equals(MappingState.BRIEFING) || currentState.equals(MappingState.ERROR)) {
                    return MappingState.ADVICES;
                }
                break;
            case ADVICES_ENDED:
                if (currentState.equals(MappingState.ADVICES)) {
                    return MappingState.MAPPING;
                }
                break;
            case MAPPING_SUCCEEDED:
                if (currentState.equals(MappingState.MAPPING)) {
                    return MappingState.SAVING_MAP;
                }
                break;
            case MAPPING_FAILED:
                if (currentState.equals(MappingState.MAPPING)) {
                    return MappingState.ERROR;
                }
                break;
            case SAVING_MAP_SUCCEEDED:
                return MappingState.SUCCESS;
            case SAVING_MAP_FAILED:
                if (currentState.equals(MappingState.SAVING_MAP)) {
                    return MappingState.ERROR;
                }
                break;
            case SUCCESS_CONFIRMED:
                if (currentState.equals(MappingState.SUCCESS)) {
                    return MappingState.END;
                }
                break;
        }

        return currentState;
    }
}
