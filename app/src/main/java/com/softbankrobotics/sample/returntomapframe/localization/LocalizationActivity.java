/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.returntomapframe.localization;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.GoToBuilder;
import com.aldebaran.qi.sdk.builder.HolderBuilder;
import com.aldebaran.qi.sdk.object.actuation.LocalizationStatus;
import com.aldebaran.qi.sdk.object.actuation.Localize;
import com.aldebaran.qi.sdk.object.holder.AutonomousAbilitiesType;
import com.aldebaran.qi.sdk.object.holder.Holder;
import com.softbankrobotics.sample.returntomapframe.core.MapManager;
import com.softbankrobotics.sample.returntomapframe.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

public class LocalizationActivity extends AppCompatActivity implements RobotLifecycleCallbacks {

    private static final String TAG = "LocalizationActivity";

    @BindView(R.id.startLocalizationButton)
    Button startLocalizationButton;

    @BindView(R.id.goToMapFrameButton)
    Button goToMapFrameButton;

    @BindView(R.id.localizationProgressBar)
    ProgressBar localizationProgressBar;

    @BindView(R.id.goToProgressBar)
    ProgressBar goToProgressBar;

    @NonNull
    private final BehaviorSubject<LocalizationState> subject = BehaviorSubject.createDefault(LocalizationState.NOT_READY);
    @Nullable
    private Disposable disposable;

    @Nullable
    private QiContext qiContext;
    @Nullable
    private Localize localize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_localization);
        ButterKnife.bind(this);

        QiSDK.register(this, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        activateImmersiveMode();

        disposable = subject.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .distinctUntilChanged()
                .subscribe(this::onLocalizationStateChanged);
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
        QiSDK.unregister(this, this);
        super.onDestroy();
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        this.qiContext = qiContext;
        subject.onNext(LocalizationState.READY);
    }

    @Override
    public void onRobotFocusLost() {
        this.qiContext = null;
        subject.onNext(LocalizationState.NOT_READY);
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        Log.e(TAG, "onRobotFocusRefused: " + reason);
    }

    @OnClick(R.id.startLocalizationButton)
    public void onClickStartLocalization() {
        startLocalization();
    }

    @OnClick(R.id.goToMapFrameButton)
    public void onClickGoToMapFrame() {
        goToMapFrame();
    }

    private void startLocalization() {
        if (qiContext == null) {
            Log.e(TAG, "Error while localizing: qiContext is null");
            return;
        }

        subject.onNext(LocalizationState.LOCALIZING);

        // Hold basic awareness to avoid robot from tracking humans with his head (see issue #41596).
        Holder basicAwarenessHolder = HolderBuilder.with(qiContext)
                .withAutonomousAbilities(AutonomousAbilitiesType.BASIC_AWARENESS)
                .build();

        basicAwarenessHolder.async().hold()
                .andThenCompose(ignored -> retrieveLocalize(qiContext))
                .andThenCompose(loc -> {
                    Log.d(TAG, "Localize retrieved successfully");

                    loc.setOnStatusChangedListener(status -> {
                        if (status == LocalizationStatus.LOCALIZED) {
                            Log.d(TAG, "Robot is localized");
                            subject.onNext(LocalizationState.LOCALIZED);
                            basicAwarenessHolder.async().release();
                        }
                    });

                    Log.d(TAG, "Running Localize...");
                    return loc.async().run();
                })
                .thenConsume(future -> {
                    basicAwarenessHolder.async().release();

                    if (localize != null) {
                        localize.setOnStatusChangedListener(null);
                    }

                    if (future.hasError()) {
                        Log.e(TAG, "Error while localizing", future.getError());
                        runOnUiThread(() -> Toast.makeText(this, "Error while localizing", Toast.LENGTH_SHORT).show());
                    }

                    if (qiContext != null) {
                        subject.onNext(LocalizationState.READY);
                    } else {
                        subject.onNext(LocalizationState.NOT_READY);
                    }
                });
    }

    private void goToMapFrame() {
        if (qiContext == null) {
            Log.e(TAG, "Error while going to map frame: qiContext is null");
            return;
        }

        subject.onNext(LocalizationState.MOVING);

        qiContext.getMapping().async().mapFrame()
                .andThenCompose(mapFrame -> GoToBuilder.with(qiContext).withFrame(mapFrame).buildAsync())
                .andThenCompose(goTo -> goTo.async().run())
                .thenConsume(future -> {
                    if (future.isSuccess()) {
                        Log.d(TAG, "Map frame reached successfully");
                        subject.onNext(LocalizationState.LOCALIZED);
                    } else if (future.hasError()) {
                        Log.e(TAG, "Error while going to map frame", future.getError());
                        runOnUiThread(() -> Toast.makeText(this, "Error while going to map frame", Toast.LENGTH_SHORT).show());

                        if (qiContext != null) {
                            if (localize != null && localize.getStatus() == LocalizationStatus.LOCALIZED) {
                                subject.onNext(LocalizationState.LOCALIZED);
                            } else {
                                subject.onNext(LocalizationState.READY);
                            }
                        } else {
                            subject.onNext(LocalizationState.NOT_READY);
                        }
                    }
                });
    }

    @NonNull
    private Future<Localize> retrieveLocalize(@NonNull QiContext qiContext) {
        // Do not reuse cached Localize (see issue #41704).
        /*
        if (localize != null) {
            return Future.of(localize);
        }
        */

        Log.d(TAG, "Retrieving map...");
        return MapManager.getInstance().retrieveMap(qiContext)
                .andThenCompose(map -> {
                    Log.d(TAG, "Map retrieved successfully");
                    Log.d(TAG, "Building Localize...");
                    return qiContext.getMapping().async().makeLocalize(qiContext.getRobotContext(), map);
                })
                .andThenApply(loc -> {
                    Log.d(TAG, "Localize built successfully");

                    localize = loc;
                    return localize;
                });
    }

    private void onLocalizationStateChanged(@NonNull LocalizationState localizationState) {
        Log.d(TAG, "onLocalizationStateChanged: " + localizationState);

        switch (localizationState) {
            case NOT_READY:
            case LOCALIZING:
                startLocalizationButton.setEnabled(false);
                localizationProgressBar.setVisibility(View.VISIBLE);
                goToMapFrameButton.setEnabled(false);
                goToProgressBar.setVisibility(View.GONE);
                break;
            case MOVING:
                startLocalizationButton.setEnabled(false);
                localizationProgressBar.setVisibility(View.GONE);
                goToMapFrameButton.setEnabled(false);
                goToProgressBar.setVisibility(View.VISIBLE);
                break;
            case READY:
                startLocalizationButton.setEnabled(true);
                localizationProgressBar.setVisibility(View.GONE);
                goToMapFrameButton.setEnabled(false);
                goToProgressBar.setVisibility(View.GONE);
                break;
            case LOCALIZED:
                startLocalizationButton.setEnabled(false);
                localizationProgressBar.setVisibility(View.GONE);
                goToMapFrameButton.setEnabled(true);
                goToProgressBar.setVisibility(View.GONE);
                break;
        }
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
