/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.returntomapframe.localization;

import android.util.Log;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.Promise;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.LocalizeBuilder;
import com.aldebaran.qi.sdk.object.actuation.LocalizationStatus;
import com.aldebaran.qi.sdk.object.actuation.Localize;
import com.softbankrobotics.sample.returntomapframe.core.MapManager;
import com.softbankrobotics.sample.returntomapframe.utils.FutureCancellations;

import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Manager that starts the localization.
 */
public class LocalizeManager {

    @NonNull
    private static final String TAG = "LocalizeManager";

    @NonNull
    private final AtomicBoolean isLocalized = new AtomicBoolean(false);

    @Nullable
    private Localize localize;
    @Nullable
    private Future<Void> localization;

    /**
     * Indicate if the robot is localized or not.
     *
     * @return {@code true} if the robot is localized, {@code false} otherwise.
     */
    public boolean isLocalized() {
        return isLocalized.get();
    }

    /**
     * Indicates if the map is loaded.
     *
     * @return {@code true} if the map is loaded, {@code false} otherwise.
     */
    public boolean mapIsLoaded() {
        return localize != null;
    }

    /**
     * Load the map and create the {@link Localize} action.
     *
     * @param qiContext the qiContext
     * @return A {@link Future} wrapping the operation.
     */
    @NonNull
    public Future<Void> loadMap(@NonNull QiContext qiContext) {
        return MapManager.getInstance().retrieveMap(qiContext)
                .andThenCompose(map -> {
                    Log.d(TAG, "Map retrieved successfully");
                    Log.d(TAG, "Building Localize...");
                    return LocalizeBuilder.with(qiContext)
                            .withMap(map)
                            .buildAsync();
                })
                .andThenConsume(loc -> {
                    Log.d(TAG, "Localize built successfully");
                    localize = loc;
                });
    }

    /**
     * Localize the robot. This method starts the localization and waits for the robot to be localized.
     * Once localized, the operation is considered as successful and the robot stays localized until the localization is cancelled or encounters an error.
     *
     * @return A {@link Future} wrapping the operation.
     * This operation is a success when the robot is localized.
     * If the {@link Localize} action is cancelled before that, the operation is cancelled.
     * If the {@link Localize} action encounters an error before that, the operation fails.
     */
    @NonNull
    public Future<Void> localizeRobot() {
        // Promise used to set the operation result.
        Promise<Void> promise = new Promise<>();

        // Cancel the running localization if any, and start a new localization.
        FutureCancellations.cancel(localization)
                .andThenCompose(ignored -> {
                    if (localize == null) {
                        throw new IllegalStateException("localize is null");
                    }

                    localize.addOnStatusChangedListener(status -> {
                        if (status == LocalizationStatus.LOCALIZED) {
                            Log.d(TAG, "Robot is localized");
                            isLocalized.set(true);
                            if (!promise.getFuture().isDone()) {
                                // Once the robot is localized, consider the operation as a success.
                                promise.setValue(null);
                            }
                        }
                    });

                    Log.d(TAG, "Running Localize...");
                    localization = localize.async().run();
                    return localization;
                })
                .thenConsume(future -> {
                    isLocalized.set(false);

                    if (localize != null) {
                        localize.removeAllOnStatusChangedListeners();
                    }

                    if (future.hasError()) {
                        Log.e(TAG, "Error while localizing", future.getError());
                        if (!promise.getFuture().isDone()) {
                            // Consider the operation as a failure.
                            promise.setError(future.getErrorMessage());
                        }
                    } else if (future.isCancelled()) {
                        if (!promise.getFuture().isDone()) {
                            // Consider the operation has been cancelled.
                            promise.setCancelled();
                        }
                    }
                });

        // Return the future associated with the promise.
        return promise.getFuture();
    }
}
