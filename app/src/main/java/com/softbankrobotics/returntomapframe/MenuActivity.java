/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.returntomapframe;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

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
}
