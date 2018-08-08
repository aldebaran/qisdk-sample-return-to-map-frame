/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.returntomapframe.localization;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;

public interface Screen {
    @StringRes int getTitle();
    void start(@NonNull QiContext qiContext);
    @NonNull Future<Void> stop();
}
