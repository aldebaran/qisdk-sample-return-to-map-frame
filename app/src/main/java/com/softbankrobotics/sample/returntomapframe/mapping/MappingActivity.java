/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.returntomapframe.mapping;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.softbankrobotics.sample.returntomapframe.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RawRes;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * The mapping Activity.
 */
public class MappingActivity extends RobotActivity {

    @NonNull
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

    @BindView(R.id.infoImageView)
    ImageView infoImageView;

    @BindView(R.id.progressAnimationView)
    LottieAnimationView progressAnimationView;

    @Nullable
    private Disposable disposable;

    @Nullable
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mapping);
        ButterKnife.bind(this);

        QiSDK.register(this, robot);
    }

    @Override
    protected void onResume() {
        super.onResume();

        infoTextView.setVisibility(View.INVISIBLE);
        startMappingButton.setVisibility(View.INVISIBLE);
        warningImage.setVisibility(View.INVISIBLE);
        infoImageView.setVisibility(View.INVISIBLE);
        progressAnimationView.setVisibility(View.INVISIBLE);

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

    private void playSound(@RawRes int soundResId, boolean playInLoop) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        mediaPlayer = MediaPlayer.create(this, soundResId);
        mediaPlayer.setLooping(playInLoop);
        mediaPlayer.start();
    }

    private void onMappingStateChanged(@NonNull MappingState mappingState) {
        Log.d(TAG, "onMappingStateChanged: " + mappingState);

        switch (mappingState) {
            case IDLE:
                infoTextView.setVisibility(View.INVISIBLE);
                startMappingButton.setVisibility(View.INVISIBLE);
                warningImage.setVisibility(View.GONE);
                infoImageView.setVisibility(View.INVISIBLE);
                progressAnimationView.setVisibility(View.INVISIBLE);
                break;
            case BRIEFING:
                infoTextView.setVisibility(View.VISIBLE);
                startMappingButton.setVisibility(View.VISIBLE);
                warningImage.setVisibility(View.GONE);
                infoImageView.setVisibility(View.INVISIBLE);
                progressAnimationView.setVisibility(View.INVISIBLE);
                infoTextView.setText(R.string.briefing_text);
                break;
            case ADVICES:
                infoTextView.setVisibility(View.VISIBLE);
                startMappingButton.setVisibility(View.INVISIBLE);
                warningImage.setVisibility(View.GONE);
                infoImageView.setVisibility(View.VISIBLE);
                progressAnimationView.setVisibility(View.INVISIBLE);
                infoTextView.setText(R.string.advices_text);
                infoImageView.setImageResource(R.drawable.hiding);
                break;
            case MAPPING:
                infoTextView.setVisibility(View.VISIBLE);
                startMappingButton.setVisibility(View.INVISIBLE);
                warningImage.setVisibility(View.GONE);
                infoImageView.setVisibility(View.INVISIBLE);
                progressAnimationView.setVisibility(View.VISIBLE);
                infoTextView.setText(R.string.mapping_mapping_text);
                playSound(R.raw.sonar, true);
                break;
            case SAVING_MAP:
                infoTextView.setVisibility(View.VISIBLE);
                startMappingButton.setVisibility(View.INVISIBLE);
                warningImage.setVisibility(View.GONE);
                infoImageView.setVisibility(View.INVISIBLE);
                progressAnimationView.setVisibility(View.VISIBLE);
                infoTextView.setText(R.string.mapping_saving_map_text);
                playSound(R.raw.processing, true);
                break;
            case ERROR:
                infoTextView.setVisibility(View.VISIBLE);
                startMappingButton.setVisibility(View.VISIBLE);
                warningImage.setVisibility(View.VISIBLE);
                infoImageView.setVisibility(View.INVISIBLE);
                progressAnimationView.setVisibility(View.INVISIBLE);
                infoTextView.setText(R.string.error_text);
                playSound(R.raw.error, false);
                break;
            case SUCCESS:
                infoTextView.setVisibility(View.VISIBLE);
                startMappingButton.setVisibility(View.INVISIBLE);
                warningImage.setVisibility(View.GONE);
                infoImageView.setVisibility(View.VISIBLE);
                progressAnimationView.setVisibility(View.INVISIBLE);
                infoTextView.setText(R.string.success_text);
                infoImageView.setImageResource(R.drawable.ic_check);
                playSound(R.raw.success, false);
                break;
            case END:
                finish();
                break;
        }
    }
}
