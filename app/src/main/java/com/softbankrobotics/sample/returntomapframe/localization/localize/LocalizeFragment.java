/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.returntomapframe.localization.localize;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.softbankrobotics.sample.returntomapframe.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * The localize Fragment.
 */
public class LocalizeFragment extends Fragment {

    private static final String TAG = "LocalizeFragment";

    @Nullable
    private LocalizeScreen screen;
    @Nullable
    private LocalizeMachine machine;

    @Nullable
    private Unbinder unbinder;

    @BindView(R.id.startLocalizeButton)
    Button startLocalizeButton;

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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_localize, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        infoTextView.setVisibility(View.INVISIBLE);
        startLocalizeButton.setVisibility(View.INVISIBLE);
        warningImage.setVisibility(View.INVISIBLE);
        infoImageView.setVisibility(View.INVISIBLE);
        progressAnimationView.setVisibility(View.INVISIBLE);

        if (machine != null) {
            disposable = machine.localizeState()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::onLocalizeStateChanged);
        }
    }

    @Override
    public void onPause() {
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
    public void onDestroyView() {
        if (unbinder != null) {
            unbinder.unbind();
        }
        super.onDestroyView();
    }

    @OnClick(R.id.startLocalizeButton)
    public void onClickStartLocalize() {
        if (machine != null) {
            machine.post(LocalizeEvent.START_LOCALIZE);
        }
    }

    @NonNull
    static LocalizeFragment newInstance(@NonNull LocalizeScreen screen, @NonNull LocalizeMachine machine) {
        LocalizeFragment fragment = new LocalizeFragment();
        fragment.screen = screen;
        fragment.machine = machine;
        return fragment;
    }

    private void playSound(@RawRes int soundResId, boolean playInLoop) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        FragmentActivity activity = getActivity();
        if (activity != null) {
            mediaPlayer = MediaPlayer.create(activity, soundResId);
            mediaPlayer.setLooping(playInLoop);
            mediaPlayer.start();
        }
    }

    private void onLocalizeStateChanged(@NonNull LocalizeState localizeState) {
        Log.d(TAG, "onLocalizeStateChanged: " + localizeState);

        switch (localizeState) {
            case IDLE:
                infoTextView.setVisibility(View.INVISIBLE);
                startLocalizeButton.setVisibility(View.INVISIBLE);
                warningImage.setVisibility(View.GONE);
                infoImageView.setVisibility(View.INVISIBLE);
                progressAnimationView.setVisibility(View.INVISIBLE);
                break;
            case BRIEFING:
                infoTextView.setVisibility(View.VISIBLE);
                startLocalizeButton.setVisibility(View.VISIBLE);
                warningImage.setVisibility(View.GONE);
                infoImageView.setVisibility(View.INVISIBLE);
                progressAnimationView.setVisibility(View.INVISIBLE);
                infoTextView.setText(R.string.briefing_text);
                break;
            case ADVICES:
                infoTextView.setVisibility(View.VISIBLE);
                startLocalizeButton.setVisibility(View.INVISIBLE);
                warningImage.setVisibility(View.GONE);
                infoImageView.setVisibility(View.VISIBLE);
                progressAnimationView.setVisibility(View.INVISIBLE);
                infoTextView.setText(R.string.advices_text);
                infoImageView.setImageResource(R.drawable.hiding);
                break;
            case LOADING_MAP:
                infoTextView.setVisibility(View.VISIBLE);
                startLocalizeButton.setVisibility(View.INVISIBLE);
                warningImage.setVisibility(View.GONE);
                infoImageView.setVisibility(View.INVISIBLE);
                progressAnimationView.setVisibility(View.VISIBLE);
                infoTextView.setText(R.string.localize_loading_map_text);
                playSound(R.raw.processing, true);
                break;
            case LOCALIZING:
                infoTextView.setVisibility(View.VISIBLE);
                startLocalizeButton.setVisibility(View.INVISIBLE);
                warningImage.setVisibility(View.GONE);
                infoImageView.setVisibility(View.INVISIBLE);
                progressAnimationView.setVisibility(View.VISIBLE);
                infoTextView.setText(R.string.localize_localizing_text);
                playSound(R.raw.sonar, true);
                break;
            case ERROR:
                infoTextView.setVisibility(View.VISIBLE);
                startLocalizeButton.setVisibility(View.VISIBLE);
                warningImage.setVisibility(View.VISIBLE);
                infoImageView.setVisibility(View.INVISIBLE);
                progressAnimationView.setVisibility(View.INVISIBLE);
                infoTextView.setText(R.string.error_text);
                playSound(R.raw.error, false);
                break;
            case SUCCESS:
                infoTextView.setVisibility(View.VISIBLE);
                startLocalizeButton.setVisibility(View.INVISIBLE);
                warningImage.setVisibility(View.GONE);
                infoImageView.setVisibility(View.VISIBLE);
                progressAnimationView.setVisibility(View.INVISIBLE);
                infoTextView.setText(R.string.success_text);
                infoImageView.setImageResource(R.drawable.ic_check);
                playSound(R.raw.success, false);
                break;
            case END:
                if (screen != null) {
                    screen.onLocalizeEnd();
                }
                break;
        }
    }
}
