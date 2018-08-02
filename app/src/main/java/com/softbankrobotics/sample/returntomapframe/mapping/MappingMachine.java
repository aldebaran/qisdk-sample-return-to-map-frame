/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.returntomapframe.mapping;

import android.support.annotation.NonNull;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

class MappingMachine {

    @NonNull
    private final BehaviorSubject<MappingUiState> subject = BehaviorSubject.createDefault(MappingUiState.IDLE);

    void post(@NonNull MappingUiEvent event) {
        MappingUiState currentState = subject.getValue();
        if (currentState == null) {
            throw new IllegalStateException("MappingMachine must have a MappingUiState to be able to handle a MappingUiEvent.");
        }

        MappingUiState newState = reduce(currentState, event);
        subject.onNext(newState);
    }

    @NonNull
    Observable<MappingUiState> mappingState() {
        return subject.distinctUntilChanged();
    }

    @NonNull
    private MappingUiState reduce(@NonNull MappingUiState currentState, @NonNull MappingUiEvent event) {
        switch (event) {
            case FOCUS_GAINED:
                if (currentState.equals(MappingUiState.IDLE)) {
                    return MappingUiState.BRIEFING;
                }
                break;
            case FOCUS_LOST:
                return MappingUiState.IDLE;
            case START_MAPPING:
                if (currentState.equals(MappingUiState.BRIEFING) || currentState.equals(MappingUiState.ERROR)) {
                    return MappingUiState.MAPPING;
                }
                break;
            case MAPPING_SUCCEEDED:
                if (currentState.equals(MappingUiState.MAPPING)) {
                    return MappingUiState.SUCCESS;
                }
                break;
            case MAPPING_FAILED:
                if (currentState.equals(MappingUiState.MAPPING)) {
                    return MappingUiState.ERROR;
                }
                break;
            case MAPPING_SUCCESS_CONFIRMED:
                if (currentState.equals(MappingUiState.SUCCESS)) {
                    return MappingUiState.END;
                }
                break;
        }

        return currentState;
    }
}
