/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.returntomapframe.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.aldebaran.qi.Future;

import java.util.ArrayList;
import java.util.List;

/**
 * Provide utility methods to cancel futures and to be notified when the cancellation is done.
 */
public final class FutureCancellations {

    private FutureCancellations() {}

    /**
     * Cancel all the provided futures.
     *
     * @param futuresToCancel the futures to cancel
     * @return A {@link Future} that can only end in a success state, when all the provided futures are cancelled.
     * If the futures to cancel are already done, this method returns immediately.
     */
    @NonNull
    public static Future<Void> cancel(@Nullable Future<?>... futuresToCancel) {
        if (futuresToCancel == null) {
            return Future.of(null);
        }

        List<Future<?>> cancellations = new ArrayList<>();

        for (Future<?> futureToCancel : futuresToCancel) {
            Future<Void> cancellation = cancelFuture(futureToCancel);
            cancellations.add(cancellation);
        }

        Future<?>[] cancellationsArray = new Future<?>[cancellations.size()];
        return Future.waitAll(cancellations.toArray(cancellationsArray));
    }

    /**
     * Cancel the provided {@link Future}.
     *
     * @param futureToCancel the {@link Future} to cancel
     * @return A {@link Future} that can only end in a success state, when the provided {@link Future} is cancelled.
     * If the {@link Future} to cancel is already done, this method returns immediately.
     */
    @NonNull
    private static Future<Void> cancelFuture(@Nullable Future<?> futureToCancel) {
        if (futureToCancel == null) {
            return Future.of(null);
        }

        futureToCancel.requestCancellation();
        return futureToCancel.thenConsume(future -> {});
    }
}
