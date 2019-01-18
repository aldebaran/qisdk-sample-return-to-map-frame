package com.softbankrobotics.sample.returntomapframe.GotoAB;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.GoToBuilder;
import com.aldebaran.qi.sdk.builder.TransformBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.object.actuation.Actuation;
import com.aldebaran.qi.sdk.object.actuation.Frame;
import com.aldebaran.qi.sdk.object.actuation.FreeFrame;
import com.aldebaran.qi.sdk.object.actuation.GoTo;
import com.aldebaran.qi.sdk.object.actuation.Mapping;
import com.aldebaran.qi.sdk.object.geometry.Transform;
import com.softbankrobotics.sample.returntomapframe.R;

import java.util.concurrent.TimeUnit;

public class GotoWorldActivity extends RobotActivity implements RobotLifecycleCallbacks {
    private static final String TAG = "GotoWorldActivity";
    private TextView textView;
    private GoTo goTo;
    private QiContext qiContext;
    private Actuation actuation;
    private Mapping mapping;
    private Frame robotFrame;
    private FreeFrame targetFrame;
    private Boolean flag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_go_to_world);

        this.textView = findViewById(R.id.textView4);
        Button button = findViewById(R.id.button);

        button.setOnClickListener(v -> handleGotoClick());

        QiSDK.register(this, this);
    }

    private void handleGotoClick() {

        flag = transform(12.0, 5.0, 0.0);
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (flag)
            flag = transform(6.0, 0.0, 90.0);
        else
            flag = transform(0.5, 0.0, 0.0);

        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (flag)
            flag = transform(6.0, 0.0, 90.0);
        else
            flag = transform(0.5, 0.0, 0.0);

        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (flag)
            flag = transform(6.0, 0.0, 90.0);
        else
            flag = transform(0.5, 0.0, 0.0);

        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Boolean transform(double x, double y, double z) {
        double rotValue;

        if (z == 0.0)
            rotValue = 0.0;
        else
            rotValue = (z * ((22 / 7) / 180));

        Thread thread = new Thread(() -> {
            this.robotFrame = this.actuation.robotFrame();
            this.targetFrame = this.mapping.makeFreeFrame();
            try {

                Transform transform = TransformBuilder.create().from2DTransform(x, y, rotValue);

                this.targetFrame.update(this.robotFrame, transform, 0L);

                this.goTo = GoToBuilder.with(this.qiContext)
                        .withFrame(this.targetFrame.frame())
                        .build();


                this.goTo.addOnStartedListener(() -> {
                    String message = "GoTo action started.";
                    runOnUiThread(() -> this.textView.setText(message));
                    Log.i(TAG, message);
                });

                Future<Void> goToFuture = goTo.async().run();
                goToFuture.thenConsume(future -> {
                    if (future.isSuccess()) {
                        String message = "GoTo action finished with success.";
                        runOnUiThread(() -> this.textView.setText(message));
                        Log.i(TAG, message);
                        this.flag = true;
                    } else if (future.hasError()) {
                        String message = "GoTo action finished with error.";
                        runOnUiThread(() -> this.textView.setText(message));
                        this.flag = false;
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
        return this.flag;
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        Log.i(TAG, "Focus gained.");
        // Store the provided QiContext and services.
        this.qiContext = qiContext;

        this.actuation = qiContext.getActuation();
        this.mapping = qiContext.getMapping();
    }

    @Override
    public void onRobotFocusLost() {
        Log.i(TAG, "Focus lost.");
        // Remove the QiContext.
        qiContext = null;

        if (goTo != null) {
            goTo.removeAllOnStartedListeners();
        }
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        // Nothing here.
    }
}
