/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.returntomapframe.mapping;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
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

    @BindView(R.id.infoTextView)
    TextView infoTextView;

    @BindView(R.id.warningImage)
    ImageView warningImage;

    @BindView(R.id.successImage)
    ImageView successImage;

    @BindView(R.id.progressAnimationView)
    LottieAnimationView progressAnimationView;

    @Nullable
    private Disposable disposable;

    @Nullable
    private MediaPlayer mediaPlayer;

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

        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
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

    @OnClick(R.id.closeButton)
    public void onCloseClicked() {
        finishAffinity();
    }

    @OnClick(R.id.backButton)
    public void onBackClicked() {
        onBackPressed();
    }

    private void playSound(@RawRes int soundResId) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        mediaPlayer = MediaPlayer.create(this, soundResId);
        mediaPlayer.start();
    }

    private void onMappingStateChanged(@NonNull MappingState mappingState) {
        Log.d(TAG, "onMappingStateChanged: " + mappingState);

        switch (mappingState) {
            case IDLE:
                infoTextView.setVisibility(View.INVISIBLE);
                startMappingButton.setVisibility(View.INVISIBLE);
                warningImage.setVisibility(View.GONE);
                successImage.setVisibility(View.INVISIBLE);
                progressAnimationView.setVisibility(View.INVISIBLE);
                break;
            case BRIEFING:
                infoTextView.setVisibility(View.VISIBLE);
                startMappingButton.setVisibility(View.VISIBLE);
                warningImage.setVisibility(View.GONE);
                successImage.setVisibility(View.INVISIBLE);
                infoTextView.setText(R.string.mapping_briefing_text);
                progressAnimationView.setVisibility(View.INVISIBLE);
                break;
            case MAPPING:
                infoTextView.setVisibility(View.VISIBLE);
                startMappingButton.setVisibility(View.INVISIBLE);
                warningImage.setVisibility(View.GONE);
                successImage.setVisibility(View.INVISIBLE);
                infoTextView.setText(R.string.mapping_mapping_text);
                progressAnimationView.setVisibility(View.VISIBLE);
                break;
            case SAVING_MAP:
                infoTextView.setVisibility(View.VISIBLE);
                startMappingButton.setVisibility(View.INVISIBLE);
                warningImage.setVisibility(View.GONE);
                successImage.setVisibility(View.INVISIBLE);
                infoTextView.setText(R.string.mapping_saving_map_text);
                progressAnimationView.setVisibility(View.VISIBLE);
                playSound(R.raw.success);
                break;
            case ERROR:
                infoTextView.setVisibility(View.VISIBLE);
                startMappingButton.setVisibility(View.VISIBLE);
                warningImage.setVisibility(View.VISIBLE);
                successImage.setVisibility(View.INVISIBLE);
                infoTextView.setText(R.string.mapping_error_text);
                progressAnimationView.setVisibility(View.INVISIBLE);
                playSound(R.raw.error);
                break;
            case SUCCESS:
                infoTextView.setVisibility(View.VISIBLE);
                startMappingButton.setVisibility(View.INVISIBLE);
                warningImage.setVisibility(View.GONE);
                successImage.setVisibility(View.VISIBLE);
                infoTextView.setText(R.string.mapping_success_text);
                progressAnimationView.setVisibility(View.INVISIBLE);
                playSound(R.raw.success);
                break;
            case END:
                finish();
                break;
        }
    }
}
