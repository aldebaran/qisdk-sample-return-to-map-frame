/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.returntomapframe.mapping;

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
import com.aldebaran.qi.sdk.object.actuation.LocalizationStatus;
import com.aldebaran.qi.sdk.object.actuation.LocalizeAndMap;
import com.softbankrobotics.returntomapframe.core.MapManager;
import com.softbankrobotics.returntomapframe.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

public class MappingActivity extends AppCompatActivity implements RobotLifecycleCallbacks {

    private static final String TAG = "MappingActivity";

    @BindView(R.id.startMappingButton)
    Button startMappingButton;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @NonNull
    private final BehaviorSubject<MappingState> subject = BehaviorSubject.createDefault(MappingState.NOT_READY);
    @Nullable
    private Disposable disposable;

    @Nullable
    private QiContext qiContext;
    @Nullable
    private LocalizeAndMap localizeAndMap;
    @Nullable
    private Future<Void> mapping;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapping);
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
        QiSDK.unregister(this, this);
        super.onDestroy();
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        this.qiContext = qiContext;
        subject.onNext(MappingState.READY);
    }

    @Override
    public void onRobotFocusLost() {
        this.qiContext = null;
        subject.onNext(MappingState.NOT_READY);
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        Log.e(TAG, "onRobotFocusRefused: " + reason);
    }

    @OnClick(R.id.startMappingButton)
    public void onClickStartMapping() {
        startMapping();
    }

    private void startMapping() {
        if (qiContext == null) {
            Log.e(TAG, "Error while mapping: qiContext is null");
            return;
        }

        subject.onNext(MappingState.MAPPING);

        mapping = qiContext.getMapping().async().makeLocalizeAndMap(qiContext.getRobotContext())
                .andThenCompose(loc -> {
                    localizeAndMap = loc;

                    localizeAndMap.setOnStatusChangedListener(status -> {
                        if (status == LocalizationStatus.LOCALIZED) {
                            stopMapping();
                            saveMap();
                        }
                    });

                    return localizeAndMap.async().run();
                })
                .thenConsume(future -> {
                    if (localizeAndMap != null) {
                        localizeAndMap.setOnStatusChangedListener(null);
                    }

                    if (future.hasError()) {
                        Log.e(TAG, "Error while mapping", future.getError());
                        runOnUiThread(() -> Toast.makeText(this, "Error while mapping", Toast.LENGTH_SHORT).show());
                    }

                    if (!future.isCancelled()) {
                        if (qiContext != null) {
                            subject.onNext(MappingState.READY);
                        } else {
                            subject.onNext(MappingState.NOT_READY);
                        }
                    }
                });
    }

    private void stopMapping() {
        if (mapping != null) {
            mapping.requestCancellation();
        }
    }

    private void saveMap() {
        if (localizeAndMap == null) {
            Log.e(TAG, "Error while saving map: localizeAndMap is null");
            return;
        }

        subject.onNext(MappingState.SAVING_MAP);

        localizeAndMap.async().dumpMap()
                .andThenConsume(map -> {
                    Log.d(TAG, "Saving map...");
                    MapManager.getInstance().saveMap(getApplicationContext(), map);
                })
                .thenConsume(future -> {
                    if (future.isSuccess()) {
                        Log.d(TAG, "Map saved successfully");
                        closeScreen();
                    } else if (future.hasError()) {
                        Log.e(TAG, "Error while saving map", future.getError());

                        if (qiContext != null) {
                            subject.onNext(MappingState.READY);
                        } else {
                            subject.onNext(MappingState.NOT_READY);
                        }
                    }
                });
    }

    private void closeScreen() {
        runOnUiThread(this::finish);
    }

    private void onMappingStateChanged(@NonNull MappingState mappingState) {
        Log.d(TAG, "onMappingStateChanged: " + mappingState);

        switch (mappingState) {
            case NOT_READY:
            case MAPPING:
            case SAVING_MAP:
                startMappingButton.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);
                break;
            case READY:
                startMappingButton.setEnabled(true);
                progressBar.setVisibility(View.GONE);
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
