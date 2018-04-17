package com.softbankrobotics.returntomapframe;

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

        useMapButton.setEnabled(false);
    }

    @OnClick(R.id.createMapButton)
    public void onClickCreateMap() {
        // TODO: impl
    }

    @OnClick(R.id.useMapButton)
    public void onClickUseMap() {
        // TODO: impl
    }
}
