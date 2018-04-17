package com.softbankrobotics.returntomapframe;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.aldebaran.qi.sdk.object.actuation.ExplorationMap;

class MapManager {

    @Nullable
    private ExplorationMap explorationMap;

    private MapManager() {}

    @NonNull
    static MapManager getInstance() {
        return Holder.INSTANCE;
    }

    void saveMap(@NonNull ExplorationMap map) {
        this.explorationMap = map;
    }

    boolean hasMap() {
        return explorationMap != null;
    }

    private static final class Holder {
        @NonNull
        private static final MapManager INSTANCE = new MapManager();
    }
}
