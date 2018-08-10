/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.returntomapframe.localization.gotoorigin;

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

public class GoToOriginFragment extends Fragment {

    private static final String TAG = "GoToOriginFragment";

    @Nullable
    private GoToOriginScreen screen;
    @Nullable
    private GoToOriginMachine machine;

    @Nullable
    private Unbinder unbinder;

    @BindView(R.id.startGoToButton)
    Button startGoToButton;

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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_go_to_origin, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        infoTextView.setVisibility(View.INVISIBLE);
        startGoToButton.setVisibility(View.INVISIBLE);
        warningImage.setVisibility(View.INVISIBLE);
        successImage.setVisibility(View.INVISIBLE);
        progressAnimationView.setVisibility(View.INVISIBLE);

        if (machine != null) {
            disposable = machine.goToOriginState()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::onGoToOriginStateChanged);
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

    @OnClick(R.id.startGoToButton)
    public void onClickStartGoTo() {
        if (machine != null) {
            machine.post(GoToOriginEvent.START_GO_TO_ORIGIN);
        }
    }

    @NonNull
    static GoToOriginFragment newInstance(@NonNull GoToOriginScreen screen, @NonNull GoToOriginMachine machine) {
        GoToOriginFragment fragment = new GoToOriginFragment();
        fragment.screen = screen;
        fragment.machine = machine;
        return fragment;
    }

    private void playSound(@RawRes int soundResId) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        FragmentActivity activity = getActivity();
        if (activity != null) {
            mediaPlayer = MediaPlayer.create(activity, soundResId);
            mediaPlayer.start();
        }
    }

    private void onGoToOriginStateChanged(@NonNull GoToOriginState goToOriginState) {
        Log.d(TAG, "onGoToOriginStateChanged: " + goToOriginState);

        switch (goToOriginState) {
            case IDLE:
                infoTextView.setVisibility(View.INVISIBLE);
                startGoToButton.setVisibility(View.INVISIBLE);
                warningImage.setVisibility(View.GONE);
                successImage.setVisibility(View.INVISIBLE);
                progressAnimationView.setVisibility(View.INVISIBLE);
                break;
            case BRIEFING:
                infoTextView.setVisibility(View.VISIBLE);
                startGoToButton.setVisibility(View.VISIBLE);
                warningImage.setVisibility(View.GONE);
                successImage.setVisibility(View.INVISIBLE);
                infoTextView.setText(R.string.go_to_origin_briefing_text);
                progressAnimationView.setVisibility(View.INVISIBLE);
                break;
            case MOVING:
                infoTextView.setVisibility(View.VISIBLE);
                startGoToButton.setVisibility(View.INVISIBLE);
                warningImage.setVisibility(View.GONE);
                successImage.setVisibility(View.INVISIBLE);
                infoTextView.setText(R.string.go_to_origin_moving_text);
                progressAnimationView.setVisibility(View.VISIBLE);
                break;
            case ERROR:
                infoTextView.setVisibility(View.VISIBLE);
                startGoToButton.setVisibility(View.VISIBLE);
                warningImage.setVisibility(View.VISIBLE);
                successImage.setVisibility(View.INVISIBLE);
                infoTextView.setText(R.string.error_text);
                progressAnimationView.setVisibility(View.INVISIBLE);
                playSound(R.raw.error);
                break;
            case SUCCESS:
                infoTextView.setVisibility(View.VISIBLE);
                startGoToButton.setVisibility(View.INVISIBLE);
                warningImage.setVisibility(View.GONE);
                successImage.setVisibility(View.VISIBLE);
                infoTextView.setText(R.string.success_text);
                progressAnimationView.setVisibility(View.INVISIBLE);
                playSound(R.raw.success);
                break;
            case END:
                if (screen != null) {
                    screen.onGoToOriginEnd();
                }
                break;
        }
    }
}
