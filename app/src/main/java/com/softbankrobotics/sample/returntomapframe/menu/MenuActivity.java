/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.returntomapframe.menu;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RadioButton;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.softbankrobotics.sample.returntomapframe.R;
import com.softbankrobotics.sample.returntomapframe.core.MapManager;
import com.softbankrobotics.sample.returntomapframe.localization.LocalizationActivity;
import com.softbankrobotics.sample.returntomapframe.mapping.MappingActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MenuActivity extends RobotActivity implements RobotLifecycleCallbacks {

    private static final String TAG = "MenuActivity";

    @BindView(R.id.createMapButton)
    RadioButton createMapButton;

    @BindView(R.id.useMapButton)
    RadioButton useMapButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_menu);
        ButterKnife.bind(this);

        QiSDK.register(this, this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        createMapButton.setChecked(false);
        useMapButton.setChecked(false);

        createMapButton.setEnabled(true);
        useMapButton.setEnabled(false);

        if (MapManager.getInstance().hasMap(getApplicationContext())) {
            useMapButton.setEnabled(true);
        }
    }

    @Override
    protected void onDestroy() {
        QiSDK.unregister(this, this);
        super.onDestroy();
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {

    }

    @Override
    public void onRobotFocusLost() {

    }

    @Override
    public void onRobotFocusRefused(String reason) {
        Log.e(TAG, "onRobotFocusRefused: " + reason);
    }

    @OnClick(R.id.createMapButton)
    public void onClickCreateMap() {
        disableButtons();
        startActivity(new Intent(this, MappingActivity.class));
    }

    @OnClick(R.id.useMapButton)
    public void onClickUseMap() {
        disableButtons();
        startActivity(new Intent(this, LocalizationActivity.class));
    }

    private void disableButtons() {
        runOnUiThread(() -> {
            createMapButton.setEnabled(false);
            useMapButton.setEnabled(false);
        });
    }
}
