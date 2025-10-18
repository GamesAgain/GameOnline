package com.gameonline.model;

import java.io.Serializable;

/**
 * Lightweight description of a player used in lobby/game updates.
 */
public final class PlayerInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;
    private final String name;
    private final Lane lane;

    public PlayerInfo(int id, String name, Lane lane) {
        this.id = id;
        this.name = name;
        this.lane = lane;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Lane getLane() {
        return lane;
    }
}
