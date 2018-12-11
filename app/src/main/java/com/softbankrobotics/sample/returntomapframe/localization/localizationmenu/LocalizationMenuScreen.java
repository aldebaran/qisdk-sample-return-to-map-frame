/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.returntomapframe.localization.localizationmenu;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.softbankrobotics.sample.returntomapframe.R;
import com.softbankrobotics.sample.returntomapframe.localization.LocalizationActivity;
import com.softbankrobotics.sample.returntomapframe.localization.LocalizeManager;
import com.softbankrobotics.sample.returntomapframe.localization.Screen;
import com.softbankrobotics.sample.returntomapframe.localization.ScreenEvent;

/**
 * The localization menu screen.
 */
public class LocalizationMenuScreen implements Screen {

    @NonNull
    private final LocalizationActivity activity;
    @NonNull
    private final LocalizationMenuRobot robot;
    @Nullable
    private LocalizationMenuFragment localizationMenuFragment;

    public LocalizationMenuScreen(@NonNull LocalizationActivity activity, @NonNull LocalizeManager localizeManager) {
        this.activity = activity;
        this.robot = new LocalizationMenuRobot(this, localizeManager);
    }

    @Override
    public void start(@NonNull QiContext qiContext) {
        activity.setNavigationTitle(R.string.localization_menu_title);

        localizationMenuFragment = LocalizationMenuFragment.newInstance(this);
        activity.showFragment(localizationMenuFragment);
        robot.startDiscussion(qiContext);
    }

    @NonNull
    @Override
    public Future<Void> stop() {
        return robot.stop();
    }

    void enableGoToOrigin() {
        if (localizationMenuFragment != null) {
            localizationMenuFragment.enableGoToOrigin();
        }
    }

    void disableChoices() {
        if (localizationMenuFragment != null) {
            localizationMenuFragment.disableChoices();
        }
    }

    void selectLocalize() {
        if (localizationMenuFragment != null) {
            localizationMenuFragment.selectLocalize();
        }
    }


    void selectGoToOrigin() {
        if (localizationMenuFragment != null) {
            localizationMenuFragment.selectGoToOrigin();
        }
    }

    void onLocalizeSelected() {
        activity.getScreenMachine().post(ScreenEvent.LOCALIZE_SELECTED);
    }

    void onGoToOriginSelected() {
        activity.getScreenMachine().post(ScreenEvent.GO_TO_ORIGIN_SELECTED);
    }

    void onGoToSelected() {
        activity.getScreenMachine().post(ScreenEvent.GO_TO_ORIGIN_SELECTED);
    }

    void onLocalizeClicked() {
        if (localizationMenuFragment != null) {
            localizationMenuFragment.disableChoices();
        }
        robot.goToLocalizeBookmark();
    }


    void onGoToInitialPositionClicked() {
        if (localizationMenuFragment != null) {
            localizationMenuFragment.disableChoices();
        }
        robot.goToGoToInitialPositionBookmark();
    }
}
