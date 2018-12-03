/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.returntomapframe.localization.gotoorigin;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.softbankrobotics.sample.returntomapframe.R;
import com.softbankrobotics.sample.returntomapframe.localization.LocalizationActivity;
import com.softbankrobotics.sample.returntomapframe.localization.Screen;
import com.softbankrobotics.sample.returntomapframe.localization.ScreenEvent;

import androidx.annotation.NonNull;

/**
 * The go to origin screen.
 */
public class GoToOriginScreen implements Screen {

    @NonNull
    private final LocalizationActivity activity;
    @NonNull
    private final GoToOriginRobot robot;
    @NonNull
    private final GoToOriginMachine machine = new GoToOriginMachine();

    public GoToOriginScreen(@NonNull LocalizationActivity activity) {
        this.activity = activity;
        this.robot = new GoToOriginRobot(machine);
    }

    @Override
    public void start(@NonNull QiContext qiContext) {
        activity.setNavigationTitle(R.string.go_to_origin_title);

        GoToOriginFragment fragment = GoToOriginFragment.newInstance(this, machine);
        activity.showFragment(fragment);
        robot.start(qiContext);
    }

    @NonNull
    @Override
    public Future<Void> stop() {
        return robot.stop();
    }

    void onGoToOriginEnd() {
        activity.getScreenMachine().post(ScreenEvent.GO_TO_ORIGIN_END);
    }
}
