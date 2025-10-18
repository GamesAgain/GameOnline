package com.gameonline.engine;

/**
 * Represents a raw input sent by a client.
 */
public final class PlayerInput {
    private final int playerId;
    private final long pressTimeMillis;

    public PlayerInput(int playerId, long pressTimeMillis) {
        this.playerId = playerId;
        this.pressTimeMillis = pressTimeMillis;
    }

    public int getPlayerId() {
        return playerId;
    }

    public long getPressTimeMillis() {
        return pressTimeMillis;
    }
}
