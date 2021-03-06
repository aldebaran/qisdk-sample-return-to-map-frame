/*
 * Copyright (C) 2018 SoftBank Robotics Europe
 * See COPYING for the license
 */
package com.softbankrobotics.sample.returntomapframe.localization.localize;

/**
 * An event for {@link LocalizeScreen}.
 */
enum LocalizeEvent {
    START,
    STOP,
    START_LOCALIZE,
    ADVICES_ENDED,
    LOADING_MAP_SUCCEEDED,
    LOADING_MAP_FAILED,
    LOCALIZE_SUCCEEDED,
    LOCALIZE_FAILED,
    SUCCESS_CONFIRMED,
}
