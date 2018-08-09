/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.returntomapframe.localization.localize;

import android.support.annotation.NonNull;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.softbankrobotics.sample.returntomapframe.R;
import com.softbankrobotics.sample.returntomapframe.localization.LocalizationActivity;
import com.softbankrobotics.sample.returntomapframe.localization.LocalizeManager;
import com.softbankrobotics.sample.returntomapframe.localization.Screen;
import com.softbankrobotics.sample.returntomapframe.localization.ScreenEvent;

public class LocalizeScreen implements Screen {

    @NonNull
    private final LocalizationActivity activity;
    @NonNull
    private final LocalizeRobot robot;
    @NonNull
    private final LocalizeMachine machine;

    public LocalizeScreen(@NonNull LocalizationActivity activity, @NonNull LocalizeManager localizeManager) {
        this.activity = activity;
        this.machine = new LocalizeMachine(localizeManager);
        this.robot = new LocalizeRobot(machine, localizeManager);
    }

    @Override
    public void start(@NonNull QiContext qiContext) {
        activity.setNavigationTitle(R.string.localize_title);

        LocalizeFragment fragment = LocalizeFragment.newInstance(this, machine);
        activity.showFragment(fragment);
        robot.start(qiContext);
    }

    @NonNull
    @Override
    public Future<Void> stop() {
        return robot.stop();
    }

    void onLocalizeEnd() {
        activity.getScreenMachine().post(ScreenEvent.LOCALIZE_END);
    }
}
