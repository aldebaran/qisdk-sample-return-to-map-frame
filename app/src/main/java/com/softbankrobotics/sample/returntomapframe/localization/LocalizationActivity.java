/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.returntomapframe.localization;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayStrategy;
import com.softbankrobotics.sample.returntomapframe.R;
import com.softbankrobotics.sample.returntomapframe.localization.localizationmenu.LocalizationMenuScreen;
import com.softbankrobotics.sample.returntomapframe.localization.localize.LocalizeScreen;

import butterknife.ButterKnife;

public class LocalizationActivity extends RobotActivity implements RobotLifecycleCallbacks {

    private static final String TAG = "LocalizationActivity";

    @NonNull
    private final LocalizeManager localizeManager = new LocalizeManager();

    @Nullable
    private QiContext qiContext;

    @Nullable
    private Screen currentScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_localization);
        ButterKnife.bind(this);

        QiSDK.register(this, this);
    }

    @Override
    protected void onDestroy() {
        QiSDK.unregister(this, this);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        // Disabled.
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        this.qiContext = qiContext;
        startLocalizationMenuScreen();
    }

    @Override
    public void onRobotFocusLost() {
        this.qiContext = null;
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        Log.e(TAG, "onRobotFocusRefused: " + reason);
    }

    public void showSpeechBar() {
        runOnUiThread(() -> setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.ALWAYS));
    }

    public void hideSpeechBar() {
        runOnUiThread(() -> setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.OVERLAY));
    }

    public void showFragment(@NonNull Fragment fragment) {
        runOnUiThread(() ->
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, fragment)
                        .commit());
    }

    public void startLocalizationMenuScreen() {
        startScreen(new LocalizationMenuScreen(this, localizeManager));
    }

    public void startLocalizeScreen() {
        startScreen(new LocalizeScreen(this, localizeManager));
    }

    public void startGoToOriginScreen() {
        // TODO: impl
    }

    private void startScreen(@NonNull Screen screen) {
        if (currentScreen == null) {
            doStartScreen(screen);
            return;
        }

        currentScreen.stop().andThenConsume(ignored -> doStartScreen(screen));
    }

    private void doStartScreen(@NonNull Screen screen) {
        if (qiContext != null) {
            currentScreen = screen;
            screen.start(qiContext);
        }
    }
}
