/*
 * Copyright (C) 2018 SoftBank Robotics Europe
 * See COPYING for the licence
 */
package com.softbankrobotics.sample.returntomapframe.localization.gotoorigin;

/**
 * A state for {@link GoToOriginScreen}.
 */
enum GoToOriginState {
    IDLE,
    BRIEFING,
    MOVING,
    ERROR,
    SUCCESS,
    END,
}
