/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.returntomapframe.mapping;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Button;

import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayStrategy;
import com.softbankrobotics.sample.returntomapframe.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MappingActivity extends RobotActivity {

    private static final String TAG = "MappingActivity";

    @NonNull
    private final MappingMachine machine = new MappingMachine();

    @NonNull
    private final MappingRobot robot = new MappingRobot(machine);

    @BindView(R.id.startMappingButton)
    Button startMappingButton;

    @Nullable
    private Disposable disposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.OVERLAY);
        setContentView(R.layout.activity_mapping);
        ButterKnife.bind(this);

        QiSDK.register(this, robot);
    }

    @Override
    protected void onResume() {
        super.onResume();

        disposable = machine.mappingState()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onMappingStateChanged);
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
        QiSDK.unregister(this, robot);
        super.onDestroy();
    }

    @OnClick(R.id.startMappingButton)
    public void onClickStartMapping() {
        machine.post(MappingEvent.START_MAPPING);
    }

    private void closeScreen() {
        runOnUiThread(this::finish);
    }

    private void onMappingStateChanged(@NonNull MappingState mappingState) {
        Log.d(TAG, "onMappingStateChanged: " + mappingState);

        switch (mappingState) {
            case IDLE:
                break;
            case BRIEFING:
                break;
            case MAPPING:
                break;
            case ERROR:
                break;
            case SUCCESS:
                break;
            case END:
                break;
        }
    }
}
