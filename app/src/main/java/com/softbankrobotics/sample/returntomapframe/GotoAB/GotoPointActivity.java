package com.softbankrobotics.sample.returntomapframe.GotoAB;

import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;

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

public class GotoPointActivity extends RobotActivity implements RobotLifecycleCallbacks {
    private static final String TAG = "GotoPointActivity";
    private Button gotoButton;
    private Button saveButton;
    private EditText xCoordinate;
    private EditText yCoordinate;
    private EditText thetaInRad;

    private Double xValue;
    private Double yValue;
    private Double rotValue;
    private GoTo goTo;
    private Boolean flag = false;
    private QiContext qiContext;
    private Actuation actuation;
    // Store the Mapping service.
    private Mapping mapping;
    private Frame robotFrame;
    private FreeFrame targetFrame;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_go_to_world_tutorial);

        this.gotoButton = findViewById(R.id.goto_button);
        this.saveButton = findViewById(R.id.save_button);
        this.xCoordinate = findViewById(R.id.editText2);
        this.yCoordinate = findViewById(R.id.editText);
        this.thetaInRad = findViewById(R.id.editText3);



        /*this.xCoordinate.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
            }
            return false;
        });
        this.yCoordinate.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {

            }
            return false;
        });
        this.thetaInRad.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                    setRotValue(Double.valueOf(this.thetaInRad.getText().toString()));
            }
            return false;
        });
*/
        this.gotoButton.setOnClickListener(v -> handleGotoClick());
        this.saveButton.setOnClickListener(v -> handleSaveClick());


        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this);
    }

    private void handleSaveClick() {
        setxValue(Double.valueOf(this.xCoordinate.getText().toString()));
        setyValue(Double.valueOf(this.yCoordinate.getText().toString()));
        setRotValue(Double.valueOf(this.thetaInRad.getText().toString()));
        Log.i(TAG, "X Coordinate: " + getxValue() + "Y Coordinate: " + getyValue() + "Rotation in rad: " + getRotValue());
    }

    private void handleGotoClick() {

        setxValue(Double.valueOf(this.xCoordinate.getText().toString()));
        setyValue(Double.valueOf(this.yCoordinate.getText().toString()));
        setRotValue(Double.valueOf(this.thetaInRad.getText().toString()));
        // Get the Actuation service from the QiContext.

        // Get the robot frame.
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    robotFrame = actuation.robotFrame();
                    targetFrame = mapping.makeFreeFrame();
                    // Create a transform corresponding to a 1 meter forward translation.
                    Log.i(TAG, "goto X Coordinate: " + getxValue() + " Y Coordinate: " + getyValue() + " Rotation in rad: " + getRotValue());

                    Transform transform = TransformBuilder.create().from2DTransform(getxValue(), getyValue(), getRotValue());

                    targetFrame.update(robotFrame, transform, 0L);

                    // Create a GoTo action.
                    goTo = GoToBuilder.with(qiContext) // Create the builder with the QiContext.
                            .withFrame(targetFrame.frame()) // Set the target frame.
                            .build(); // Build the GoTo action.

                    // Add an on started listener on the GoTo action.
                    goTo.addOnStartedListener(() -> {
                        String message = "GoTo action started.";

                        Log.i(TAG, message);
                    });

                    // Execute the GoTo action asynchronously.
                    Future<Void> goToFuture = goTo.async().run();
                    // Add a lambda to the action execution.
                    goToFuture.thenConsume(future -> {
                        if (future.isSuccess()) {
                            String message = "GoTo action finished with success.";
                            Log.i(TAG, message);
                        } else if (future.hasError()) {
                            String message = "GoTo action finished with error.";
                            Log.e(TAG, message, future.getError());
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }


    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        Log.i(TAG, "Focus gained.");
        // Store the provided QiContext and services.
        this.qiContext = qiContext;

        actuation = qiContext.getActuation();
        mapping = qiContext.getMapping();



    }

    @Override
    public void onRobotFocusLost() {
        Log.i(TAG, "Focus lost.");
        // Remove the QiContext.
        qiContext = null;

        // Remove on started listeners from the GoTo action.
        if (goTo != null) {
            goTo.removeAllOnStartedListeners();
        }
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        // Nothing here.
    }

    public Double getxValue() {
        if (xValue == null)
            return 0.0;
        else
            return xValue;
    }

    public void setxValue(Double xValue) {
        this.xValue = xValue;
    }

    public Double getyValue() {
        if (yValue == null)
            return 0.0;
        else
            return yValue;
    }

    public void setyValue(Double yValue) {
        this.yValue = yValue;
    }

    public Double getRotValue() {
        if (rotValue == null)
            return 0.0;
        else
            return (rotValue * ((22/7)/180));
    }

    public void setRotValue(Double rotValue) {
        this.rotValue = rotValue;
    }
}
