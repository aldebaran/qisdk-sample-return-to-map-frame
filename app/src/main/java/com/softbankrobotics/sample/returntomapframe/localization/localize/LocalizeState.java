/*
 * Copyright (C) 2018 SoftBank Robotics Europe
 * See COPYING for the licence
 */
package com.softbankrobotics.sample.returntomapframe.localization.localize;

/**
 * A state for {@link LocalizeScreen}.
 */
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
