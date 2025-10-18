package com.gameonline.network.messages;

/**
 * Sent by clients whenever the assigned lane key is pressed.
 */
public final class PlayerInputMessage implements Message {
    private static final long serialVersionUID = 1L;

    private final int playerId;
    private final long pressTimeMillis;

    public PlayerInputMessage(int playerId, long pressTimeMillis) {
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
