/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.returntomapframe;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.GoToBuilder;
import com.aldebaran.qi.sdk.object.actuation.LocalizationStatus;
import com.aldebaran.qi.sdk.object.actuation.Localize;

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

        retrieveLocalize(qiContext)
                .andThenCompose(loc -> {
                    Log.d(TAG, "Localize retrieved successfully");

                    loc.setOnStatusChangedListener(status -> {
                        if (status == LocalizationStatus.LOCALIZED) {
                            Log.d(TAG, "Robot is localized");
                            subject.onNext(LocalizationState.LOCALIZED);
                        }
                    });

                    Log.d(TAG, "Running Localize...");
                    return loc.async().run();
                })
                .thenConsume(future -> {
                    if (localize != null) {
                        localize.setOnStatusChangedListener(null);
                    }

                    if (future.hasError()) {
                        Log.e(TAG, "Error while localizing", future.getError());
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
        if (localize != null) {
            return Future.of(localize);
        }

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
            case MOVING:
                startLocalizationButton.setEnabled(false);
                goToMapFrameButton.setEnabled(false);
                break;
            case READY:
                startLocalizationButton.setEnabled(true);
                goToMapFrameButton.setEnabled(false);
                break;
            case LOCALIZED:
                startLocalizationButton.setEnabled(false);
                goToMapFrameButton.setEnabled(true);
                break;
        }
    }
}
