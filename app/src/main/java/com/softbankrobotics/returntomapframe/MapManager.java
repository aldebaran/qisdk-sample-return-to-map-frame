package com.softbankrobotics.returntomapframe;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.aldebaran.qi.sdk.object.actuation.ExplorationMap;
import com.snatik.storage.Storage;

class MapManager {

    private static final String TAG = "MapManager";
    private static final String MAP_FILENAME = "map.txt";

    @Nullable
    private ExplorationMap explorationMap;

    private MapManager() {}

    @NonNull
    static MapManager getInstance() {
        return Holder.INSTANCE;
    }

    void saveMap(@NonNull Context context, @NonNull ExplorationMap map) {
        this.explorationMap = map;

        Log.d(TAG, "Serializing map...");
        map.async().serialize()
                .andThenConsume(data -> {
                    Log.d(TAG, "Map serialized successfully");
                    writeMapToFile(context, data);
                })
                .thenConsume(future -> {
                    if (future.hasError()) {
                        Log.e(TAG, "Error while serializing map", future.getError());
                    }
                });
    }

    boolean hasMap(@NonNull Context context) {
        if (explorationMap != null) {
            return true;
        }

        Storage storage = new Storage(context);
        return storage.isFileExist(storage.getInternalFilesDirectory() + "/" + MAP_FILENAME);
    }

    private void writeMapToFile(@NonNull Context context, @NonNull String data) {
        Storage storage = new Storage(context);
        storage.createFile(storage.getInternalFilesDirectory() + "/" + MAP_FILENAME, data);
    }

    private static final class Holder {
        @NonNull
        private static final MapManager INSTANCE = new MapManager();
    }
}
