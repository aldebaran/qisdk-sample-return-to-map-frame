/*
 * Copyright (C) 2018 SoftBank Robotics Europe
 * See COPYING for the licence
 */
package com.softbankrobotics.sample.returntomapframe.mapping;

/**
 * A state for {@link MappingActivity}.
 */
enum MappingState {
    IDLE,
    BRIEFING,
    ADVICES,
    MAPPING,
    SAVING_MAP,
    ERROR,
    SUCCESS,
    END,
}
