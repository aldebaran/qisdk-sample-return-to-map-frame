/*
 * Copyright (C) 2018 SoftBank Robotics Europe
 * See COPYING for the license
 */
package com.softbankrobotics.sample.returntomapframe.localization;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;

import androidx.annotation.NonNull;

/**
 * A screen. Coordinates a visual UI and a robotic UI.
 */
public interface Screen {
    /**
     * Meant to be called when the screen must start.
     * This is the place where visual UI should be displayed and robotic UI should start its behavior.
     *
     * @param qiContext the qiContext
     */
    void start(@NonNull QiContext qiContext);

    /**
     * Meant to be called when the screen must stop.
     * This is the place where the visual UI and the robotic UI must be stopped.
     *
     * @return A {@link Future} that is a success when the screen has correctly stopped.
     */
    @NonNull
    Future<Void> stop();
}
