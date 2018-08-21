/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.returntomapframe.localization;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.TextView;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.softbankrobotics.sample.returntomapframe.R;
import com.softbankrobotics.sample.returntomapframe.localization.gotoorigin.GoToOriginScreen;
import com.softbankrobotics.sample.returntomapframe.localization.localizationmenu.LocalizationMenuScreen;
import com.softbankrobotics.sample.returntomapframe.localization.localize.LocalizeScreen;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * The localization Activity.
 */
public class LocalizationActivity extends RobotActivity implements RobotLifecycleCallbacks {

    @NonNull
    private static final String TAG = "LocalizationActivity";

    @NonNull
    private final ScreenMachine screenMachine = new ScreenMachine();

    @NonNull
    private final LocalizeManager localizeManager = new LocalizeManager();

    @Nullable
    private QiContext qiContext;

    @Nullable
    private Screen currentScreen;

    @Nullable
    private Disposable disposable;

    @BindView(R.id.titleTextView)
    TextView titleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_localization);
        ButterKnife.bind(this);

        QiSDK.register(this, this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        disposable = screenMachine.screenState()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(this::onScreenStateChanged);
    }

    @Override
    protected void onPause() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        QiSDK.unregister(this, this);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        screenMachine.post(ScreenEvent.BACK);
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        this.qiContext = qiContext;
        screenMachine.post(ScreenEvent.FOCUS_GAINED);
    }

    @Override
    public void onRobotFocusLost() {
        this.qiContext = null;
        screenMachine.post(ScreenEvent.FOCUS_LOST);
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        Log.e(TAG, "onRobotFocusRefused: " + reason);
    }

    @OnClick(R.id.closeButton)
    public void onCloseClicked() {
        finishAffinity();
    }

    @OnClick(R.id.backButton)
    public void onBackClicked() {
        onBackPressed();
    }

    /**
     * Show the specified fragment.
     *
     * @param fragment the fragment to show
     */
    public void showFragment(@NonNull Fragment fragment) {
        runOnUiThread(() ->
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, fragment)
                        .commit());
    }

    /**
     * Provide the {@link ScreenMachine}.
     *
     * @return The {@link ScreenMachine}.
     */
    @NonNull
    public ScreenMachine getScreenMachine() {
        return screenMachine;
    }

    /**
     * Set the title in the navigation bar.
     *
     * @param titleRes the string resource for the title
     */
    public void setNavigationTitle(@StringRes int titleRes) {
        runOnUiThread(() -> titleTextView.setText(titleRes));
    }

    private void startLocalizationMenuScreen() {
        startScreen(new LocalizationMenuScreen(this, localizeManager));
    }

    private void startLocalizeScreen() {
        startScreen(new LocalizeScreen(this, localizeManager));
    }

    private void startGoToOriginScreen() {
        startScreen(new GoToOriginScreen(this));
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

    private void onScreenStateChanged(@NonNull ScreenState screenState) {
        Log.d(TAG, "onScreenStateChanged: " + screenState);

        switch (screenState) {
            case NONE:
                if (currentScreen != null) {
                    currentScreen.stop();
                    currentScreen = null;
                }
                break;
            case LOCALIZATION_MENU:
                startLocalizationMenuScreen();
                break;
            case LOCALIZE:
                startLocalizeScreen();
                break;
            case GO_TO_ORIGIN:
                startGoToOriginScreen();
                break;
            case END:
                runOnUiThread(this::finish);
                break;
        }
    }
}
