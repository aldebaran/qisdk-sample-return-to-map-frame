/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.returntomapframe.localization.gotoorigin;

import android.support.annotation.NonNull;

import com.aldebaran.qi.Future;
import com.softbankrobotics.sample.returntomapframe.localization.Robot;

class GoToOriginRobot implements Robot {

    @NonNull
    @Override
    public Future<Void> stop() {
        return null;
    }

    /*
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
                        runOnUiThread(() -> Toast.makeText(this, "Error while going to map frame", Toast.LENGTH_SHORT).show());

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
    */
}
