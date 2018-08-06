/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.returntomapframe.localization;

import android.support.annotation.NonNull;

import com.aldebaran.qi.Future;

public interface Robot {
    @NonNull Future<Void> stop();
}
