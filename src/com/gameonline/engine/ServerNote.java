package com.gameonline.engine;

import com.gameonline.model.Lane;
import com.gameonline.model.NoteData;

/**
 * Internal mutable note representation on the server.
 */
final class ServerNote {
    private final int id;
    private final Lane lane;
    private final long hitTimeMillis;
    private boolean hit;
    private boolean missed;

    ServerNote(NoteData data) {
        this.id = data.getId();
        this.lane = data.getLane();
        this.hitTimeMillis = data.getHitTimeMillis();
    }

    int getId() {
        return id;
    }

    Lane getLane() {
        return lane;
    }

    long getHitTimeMillis() {
        return hitTimeMillis;
    }

    boolean isHit() {
        return hit;
    }

    boolean isMissed() {
        return missed;
    }

    void markHit() {
        this.hit = true;
    }

    void markMissed() {
        this.missed = true;
    }
}
