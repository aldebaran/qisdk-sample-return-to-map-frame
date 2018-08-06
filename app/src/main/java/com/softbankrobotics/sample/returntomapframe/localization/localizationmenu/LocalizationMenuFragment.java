/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.returntomapframe.localization.localizationmenu;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import com.softbankrobotics.sample.returntomapframe.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class LocalizationMenuFragment extends Fragment {

    @Nullable
    private LocalizationMenuScreen screen;

    @Nullable
    private Unbinder unbinder;

    @BindView(R.id.localizeButton)
    RadioButton localizeButton;

    @BindView(R.id.goToInitialPositionButton)
    RadioButton goToInitialPositionButton;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_localization_menu, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        localizeButton.setChecked(false);
        goToInitialPositionButton.setChecked(false);

        localizeButton.setEnabled(true);
        goToInitialPositionButton.setEnabled(false);
    }

    @Override
    public void onDestroyView() {
        if (unbinder != null) {
            unbinder.unbind();
        }
        super.onDestroyView();
    }

    @OnClick(R.id.localizeButton)
    public void onClickLocalize() {
        if (screen != null) {
            screen.onLocalizeSelected();
        }
    }

    @OnClick(R.id.goToInitialPositionButton)
    public void onClickGoToInitialPosition() {
        if (screen != null) {
            screen.onGoToInitialPositionSelected();
        }
    }

    @OnClick(R.id.closeButton)
    public void onCloseClicked() {
        if (screen != null) {
            screen.onClose();
        }
    }

    @OnClick(R.id.backButton)
    public void onBackClicked() {
        if (screen != null) {
            screen.onBack();
        }
    }

    void setScreen(@NonNull LocalizationMenuScreen screen) {
        this.screen = screen;
    }

    void enableGoToOrigin() {
        runOnUiThread(() -> goToInitialPositionButton.setEnabled(true));
    }

    void disableChoices() {
        runOnUiThread(() -> {
            localizeButton.setEnabled(false);
            goToInitialPositionButton.setEnabled(false);
        });
    }

    void selectLocalize() {
        runOnUiThread(() -> localizeButton.setChecked(true));
    }

    void selectGoToOrigin() {
        runOnUiThread(() -> goToInitialPositionButton.setChecked(true));
    }

    private void runOnUiThread(@NonNull Runnable runnable) {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(runnable);
        }
    }
}
