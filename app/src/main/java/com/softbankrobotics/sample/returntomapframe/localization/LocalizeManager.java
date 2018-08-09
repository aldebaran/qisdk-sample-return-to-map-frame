/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.returntomapframe.localization;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

public class LocalizeManager {

    private static final String TAG = "LocalizeManager";

    @NonNull
    private final AtomicBoolean isLocalized = new AtomicBoolean(false);

    @Nullable
    private Localize localize;
    @Nullable
    private Future<Void> localization;

    public boolean isLocalized() {
        return isLocalized.get();
    }

    public boolean mapIsLoaded() {
        return localize != null;
    }

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

    @NonNull
    public Future<Void> startLocalizing() {
        Promise<Void> promise = new Promise<>();

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
                            promise.setError(future.getErrorMessage());
                        }
                    } else if (future.isCancelled()) {
                        if (!promise.getFuture().isDone()) {
                            promise.setCancelled();
                        }
                    }
                });

        return promise.getFuture();
    }
}
