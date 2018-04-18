package com.softbankrobotics.returntomapframe;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.GoToBuilder;
import com.aldebaran.qi.sdk.object.actuation.LocalizationStatus;
import com.aldebaran.qi.sdk.object.actuation.Localize;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class LocalizationActivity extends AppCompatActivity implements RobotLifecycleCallbacks {

    private static final String TAG = "LocalizationActivity";

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

        retrieveLocalize(qiContext)
                .andThenCompose(loc -> {
                    Log.d(TAG, "Localize retrieved successfully");

                    loc.setOnStatusChangedListener(status -> {
                        if (status == LocalizationStatus.LOCALIZED) {
                            Log.d(TAG, "Robot is localized");
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
                });
    }

    private void goToMapFrame() {
        if (qiContext == null) {
            Log.e(TAG, "Error while going to map frame: qiContext is null");
            return;
        }

        qiContext.getMapping().async().mapFrame()
                .andThenCompose(mapFrame -> GoToBuilder.with(qiContext).withFrame(mapFrame).buildAsync())
                .andThenCompose(goTo -> goTo.async().run())
                .thenConsume(future -> {
                    if (future.isSuccess()) {
                        Log.d(TAG, "Map frame reached successfully");
                    } else if (future.hasError()) {
                        Log.e(TAG, "Error while going to map frame", future.getError());
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
}
