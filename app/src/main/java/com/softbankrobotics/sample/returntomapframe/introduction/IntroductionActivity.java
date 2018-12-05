/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.returntomapframe.introduction;

import android.os.Bundle;
import android.util.Log;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.GoToBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.builder.TransformBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.object.actuation.Actuation;
import com.aldebaran.qi.sdk.object.actuation.Frame;
import com.aldebaran.qi.sdk.object.actuation.FreeFrame;
import com.aldebaran.qi.sdk.object.actuation.GoTo;
import com.aldebaran.qi.sdk.object.actuation.Mapping;
import com.aldebaran.qi.sdk.object.conversation.ConversationStatus;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.geometry.Transform;

/**
 * The activity for the GoTo tutorial.
 */
public class IntroductionActivity extends RobotActivity implements RobotLifecycleCallbacks {

    private static final String TAG = "GoToTutorialActivity";

    // Store the GoTo action.
    private GoTo goTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this);
    }

    @Override
    protected void onDestroy() {
        // Unregister the RobotLifecycleCallbacks for this Activity.
        QiSDK.unregister(this, this);
        super.onDestroy();
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        // Bind the conversational events to the view.
        ConversationStatus conversationStatus = qiContext.getConversation().status(qiContext.getRobotContext());

        Say say = SayBuilder.with(qiContext)
                .withText("I can move around: I will go 1 meter backward.")
                .build();

        say.run();

        // Get the Actuation service from the QiContext.
        Actuation actuation = qiContext.getActuation();

        // Get the robot frame.
        Frame robotFrame = actuation.robotFrame();

        // Create a transform corresponding to a 1 meter forward translation.
        Transform transform = TransformBuilder.create().fromXTranslation(2.5);

        // Get the Mapping service from the QiContext.
        Mapping mapping = qiContext.getMapping();

        // Create a FreeFrame with the Mapping service.
        FreeFrame targetFrame = mapping.makeFreeFrame();

        // Update the target location relatively to Pepper's current location.
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
                Actuation actuation1 = qiContext.getActuation();

                Frame robotFrame1 = actuation1.robotFrame();

                Transform transform1 = TransformBuilder.create().fromXTranslation(0.4);

                // Get the Mapping service from the QiContext.
                Mapping mapping1 = qiContext.getMapping();

                // Create a FreeFrame with the Mapping service.
                FreeFrame targetFrame1 = mapping1.makeFreeFrame();

                // Update the target location relatively to Pepper's current location.
                targetFrame1.update(robotFrame1, transform1, 0L);

                // Create a GoTo action.
                goTo = GoToBuilder.with(qiContext) // Create the builder with the QiContext.
                        .withFrame(targetFrame1.frame()) // Set the target frame.
                        .build(); // Build the GoTo action.
                Future<Void> goToFuture1 =goTo.async().run();

                goToFuture1.thenConsume(future1 -> {
                    if (future1.isSuccess()) {
                        String message1 = "GoTo action finished with success.";
                        Actuation actuation2 = qiContext.getActuation();

                        Frame robotFrame2 = actuation2.robotFrame();

                        Transform transform2 = TransformBuilder.create().fromXTranslation(0.8);

                        // Get the Mapping service from the QiContext.
                        Mapping mapping2 = qiContext.getMapping();

                        // Create a FreeFrame with the Mapping service.
                        FreeFrame targetFrame2 = mapping2.makeFreeFrame();

                        // Update the target location relatively to Pepper's current location.
                        targetFrame2.update(robotFrame2, transform2, 0L);

                        // Create a GoTo action.
                        goTo = GoToBuilder.with(qiContext) // Create the builder with the QiContext.
                                .withFrame(targetFrame2.frame()) // Set the target frame.
                                .build(); // Build the GoTo action.
                        Future<Void> goToFuture2 =goTo.async().run();
                            Log.i(TAG, message1);
                        } else if (future.hasError()) {
                            String message1 = "GoTo action finished with error.";
                            Log.e(TAG, message1, future.getError());
                        }
                    });

                Log.i(TAG, message);
            } else if (future.hasError()) {
                String message = "GoTo action finished with error.";
                Log.e(TAG, message, future.getError());
            }
        });
    }

    @Override
    public void onRobotFocusLost() {

        // Remove on started listeners from the GoTo action.
        if (goTo != null) {
            goTo.removeAllOnStartedListeners();
        }
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        // Nothing here.
    }
}
