/*
 * Copyright (C) 2018 SoftBank Robotics Europe
 * See COPYING for the licence
 */
package com.softbankrobotics.sample.returntomapframe.core;

import android.content.Context;
import android.util.Log;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.ExplorationMapBuilder;
import com.aldebaran.qi.sdk.object.actuation.ExplorationMap;
import com.snatik.storage.Storage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Manager that provides and saves the {@link ExplorationMap}.
 */
public class MapManager {

    @NonNull
    private static final String TAG = "MapManager";
    @NonNull
    private static final String MAP_FILENAME = "map.txt";

    // The cached map.
    @Nullable
    private ExplorationMap explorationMap;

    private MapManager() {
    }

    /**
     * Provide the unique {@link MapManager instance}.
     *
     * @return The unique {@link MapManager instance}.
     */
    @NonNull
    public static MapManager getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Save the specified map to a file.
     *
     * @param context the context
     * @param map     the map to save
     * @return A {@link Future} wrapping the operation.
     */
    @NonNull
    public Future<Void> saveMap(@NonNull Context context, @NonNull ExplorationMap map) {
        // Cache the map.
        this.explorationMap = map;

        // Serialize the map and write it to the file.
        Log.d(TAG, "Serializing map...");
        return map.async().serialize()
                .andThenConsume(data -> {
                    Log.d(TAG, "Map serialized successfully");
                    writeMapToFile(context, data);
                });
    }

    /**
     * Indicate if there is an existing map or not.
     *
     * @param context the context
     * @return {@code true} if there is a map, {@code false} otherwise.
     */
    public boolean hasMap(@NonNull Context context) {
        // If cached map available, return true directly.
        if (explorationMap != null) {
            return true;
        }

        // Check if the map file exists.
        Storage storage = new Storage(context);
        return storage.isFileExist(mapFilePath(storage));
    }

    /**
     * Provide the map.
     *
     * @param qiContext the qiContext
     * @return A {@link Future} wrapping the operation.
     */
    @NonNull
    public Future<ExplorationMap> retrieveMap(@NonNull QiContext qiContext) {
        // If cached map available, return it directly.
        if (explorationMap != null) {
            return Future.of(explorationMap);
        }

        // Read the file and create the ExplorationMap.
        return Future.of(new Storage(qiContext))
                .andThenApply(storage -> storage.readTextFile(mapFilePath(storage)))
                .andThenCompose(data -> ExplorationMapBuilder.with(qiContext).withMapString(data).buildAsync())
                .andThenApply(map -> {
                    // Cache the map.
                    explorationMap = map;
                    return explorationMap;
                });
    }

    private void writeMapToFile(@NonNull Context context, @NonNull String data) {
        Storage storage = new Storage(context);
        boolean success = storage.createFile(mapFilePath(storage), data);
        if (!success) {
            throw new IllegalStateException("Cannot write map to file");
        }
    }

    @NonNull
    private String mapFilePath(@NonNull Storage storage) {
        return storage.getInternalFilesDirectory() + "/" + MAP_FILENAME;
    }

    private static final class Holder {
        @NonNull
        private static final MapManager INSTANCE = new MapManager();
    }
}
