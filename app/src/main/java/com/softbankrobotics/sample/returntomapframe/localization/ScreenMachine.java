/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.returntomapframe.localization;

import android.support.annotation.NonNull;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public class ScreenMachine {

    @NonNull
    private final BehaviorSubject<ScreenState> subject = BehaviorSubject.createDefault(ScreenState.NONE);


    public void post(@NonNull ScreenEvent event) {
        ScreenState currentState = subject.getValue();
        if (currentState == null) {
            throw new IllegalStateException("ScreenMachine must have a ScreenState to be able to handle a ScreenEvent.");
        }

        ScreenState newState = reduce(currentState, event);
        subject.onNext(newState);
    }

    @NonNull
    Observable<ScreenState> screenState() {
        return subject.distinctUntilChanged();
    }

    @NonNull
    private ScreenState reduce(@NonNull ScreenState currentState, @NonNull ScreenEvent event) {
        switch (event) {
            case FOCUS_GAINED:
                if (currentState.equals(ScreenState.NONE)) {
                    return ScreenState.LOCALIZATION_MENU;
                }
                break;
            case FOCUS_LOST:
                return ScreenState.NONE;
            case BACK:
                switch (currentState) {
                    case LOCALIZATION_MENU:
                        return ScreenState.END;
                    case LOCALIZE:
                        return ScreenState.LOCALIZATION_MENU;
                    case GO_TO_ORIGIN:
                        return ScreenState.LOCALIZATION_MENU;
                }
                break;
            case LOCALIZE_SELECTED:
                if (currentState.equals(ScreenState.LOCALIZATION_MENU)) {
                    return ScreenState.LOCALIZE;
                }
                break;
            case GO_TO_ORIGIN_SELECTED:
                if (currentState.equals(ScreenState.LOCALIZATION_MENU)) {
                    return ScreenState.GO_TO_ORIGIN;
                }
                break;
            case LOCALIZE_END:
                if (currentState.equals(ScreenState.LOCALIZE)) {
                    return ScreenState.LOCALIZATION_MENU;
                }
                break;
            case GO_TO_ORIGIN_END:
                if (currentState.equals(ScreenState.GO_TO_ORIGIN)) {
                    return ScreenState.LOCALIZATION_MENU;
                }
                break;
        }

        return currentState;
    }
}
