/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.returntomapframe.menu;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.softbankrobotics.returntomapframe.core.MapManager;
import com.softbankrobotics.returntomapframe.R;
import com.softbankrobotics.returntomapframe.localization.LocalizationActivity;
import com.softbankrobotics.returntomapframe.mapping.MappingActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MenuActivity extends AppCompatActivity {

    @BindView(R.id.useMapButton)
    Button useMapButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        activateImmersiveMode();

        useMapButton.setEnabled(false);
        if (MapManager.getInstance().hasMap(getApplicationContext())) {
            useMapButton.setEnabled(true);
        }
    }

    @OnClick(R.id.createMapButton)
    public void onClickCreateMap() {
        startActivity(new Intent(this, MappingActivity.class));
    }

    @OnClick(R.id.useMapButton)
    public void onClickUseMap() {
        startActivity(new Intent(this, LocalizationActivity.class));
    }

    private void activateImmersiveMode() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
}
