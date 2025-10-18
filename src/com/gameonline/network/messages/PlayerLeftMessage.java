package com.gameonline.network.messages;

/**
 * Broadcast when a player disconnects before or during the game.
 */
public final class PlayerLeftMessage implements Message {
    private static final long serialVersionUID = 1L;

    private final int playerId;

    public PlayerLeftMessage(int playerId) {
        this.playerId = playerId;
    }

    public int getPlayerId() {
        return playerId;
    }
}
