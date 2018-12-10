/*
 * Copyright (C) 2018 SoftBank Robotics Europe
 * See COPYING for the license
 */
package com.softbankrobotics.sample.returntomapframe.localization.gotoorigin;

/**
 * An event for {@link GoToOriginScreen}.
 */
enum GoToOriginEvent {
    START,
    STOP,
    START_GO_TO_ORIGIN,
    GO_TO_ORIGIN_SUCCEEDED,
    GO_TO_ORIGIN_FAILED,
    SUCCESS_CONFIRMED,
}
