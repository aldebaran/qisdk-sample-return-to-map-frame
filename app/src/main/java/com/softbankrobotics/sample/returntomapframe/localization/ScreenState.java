/*
 * Copyright (C) 2018 SoftBank Robotics Europe
 * See COPYING for the licence
 */
package com.softbankrobotics.sample.returntomapframe.localization;

/**
 * A state for screens. Indicates the current screen.
 */
public enum ScreenState {
    NONE,
    LOCALIZATION_MENU,
    LOCALIZE,
    GO_TO_ORIGIN,
    END,
}
