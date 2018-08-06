/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.returntomapframe.localization.localize;

import android.support.annotation.NonNull;

import com.aldebaran.qi.Future;
import com.softbankrobotics.sample.returntomapframe.localization.Robot;

class LocalizeRobot implements Robot {

    /*
    @Nullable
    private Localize localize;
    */

    @NonNull
    @Override
    public Future<Void> stop() {
        return null;
    }

    /*
    private void startLocalization() {
        if (qiContext == null) {
            Log.e(TAG, "Error while localizing: qiContext is null");
            return;
        }

        subject.onNext(LocalizationState.LOCALIZING);

        retrieveLocalize(qiContext)
                .andThenCompose(loc -> {
                    Log.d(TAG, "Localize retrieved successfully");

                    loc.addOnStatusChangedListener(status -> {
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
                        localize.removeAllOnStatusChangedListeners();
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
                    return LocalizeBuilder.with(qiContext)
                            .withMap(map)
                            .buildAsync();
                })
                .andThenApply(loc -> {
                    Log.d(TAG, "Localize built successfully");

                    localize = loc;
                    return localize;
                });
    }
    */
}
