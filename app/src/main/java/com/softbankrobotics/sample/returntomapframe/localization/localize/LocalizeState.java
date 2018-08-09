/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.returntomapframe.localization.localize;

enum LocalizeState {
    IDLE,
    BRIEFING,
    ADVICES,
    LOADING_MAP,
    LOCALIZING,
    ERROR,
    SUCCESS,
    END,
}
