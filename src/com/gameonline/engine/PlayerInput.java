package com.gameonline.engine;

/**
 * Represents a raw input sent by a client.
 */
public final class PlayerInput {
    private final int playerId;
    private final long pressTimeMillis;
    private final int noteId;
    private final long clientDeltaMillis;

    public PlayerInput(int playerId, long pressTimeMillis, int noteId, long clientDeltaMillis) {
        this.playerId = playerId;
        this.pressTimeMillis = pressTimeMillis;
        this.noteId = noteId;
        this.clientDeltaMillis = clientDeltaMillis;
    }

    public int getPlayerId() {
        return playerId;
    }

    public long getPressTimeMillis() {
        return pressTimeMillis;
    }

    public int getNoteId() {
        return noteId;
    }

    public long getClientDeltaMillis() {
        return clientDeltaMillis;
    }

    public boolean hasClientDelta() {
        return clientDeltaMillis != Long.MIN_VALUE;
    }
}
