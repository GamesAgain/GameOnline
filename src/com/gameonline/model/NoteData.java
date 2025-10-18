package com.gameonline.model;

import java.io.Serializable;

/**
 * Immutable representation of a note shared between server and clients.
 */
public final class NoteData implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;
    private final Lane lane;
    private final long hitTimeMillis;

    public NoteData(int id, Lane lane, long hitTimeMillis) {
        this.id = id;
        this.lane = lane;
        this.hitTimeMillis = hitTimeMillis;
    }

    public int getId() {
        return id;
    }

    public Lane getLane() {
        return lane;
    }

    public long getHitTimeMillis() {
        return hitTimeMillis;
    }
}
