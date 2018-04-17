package com.softbankrobotics.returntomapframe;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.object.actuation.LocalizationStatus;
import com.aldebaran.qi.sdk.object.actuation.LocalizeAndMap;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MappingActivity extends AppCompatActivity implements RobotLifecycleCallbacks {

    private static final String TAG = "MappingActivity";

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
    protected void onDestroy() {
        QiSDK.unregister(this, this);
        super.onDestroy();
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        this.qiContext = qiContext;
    }

    @Override
    public void onRobotFocusLost() {
        this.qiContext = null;
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

        localizeAndMap.async().dumpMap()
                .andThenConsume(map -> {
                    Log.d(TAG, "Saving map...");
                    // TODO: save map
                })
                .thenConsume(future -> {
                    if (future.isSuccess()) {
                        Log.d(TAG, "Map saved successfully");
                        // TODO: change screen
                        // TODO: finish
                    } else if (future.hasError()) {
                        Log.e(TAG, "Error while saving map", future.getError());
                    }
                });
    }
}
