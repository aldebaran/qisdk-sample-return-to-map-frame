/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.returntomapframe.mapping;

/**
 * An event for {@link MappingActivity}.
 */
enum MappingEvent {
    FOCUS_GAINED,
    FOCUS_LOST,
    START_MAPPING,
    ADVICES_ENDED,
    MAPPING_SUCCEEDED,
    MAPPING_FAILED,
    SAVING_MAP_SUCCEEDED,
    SAVING_MAP_FAILED,
    SUCCESS_CONFIRMED,
}
