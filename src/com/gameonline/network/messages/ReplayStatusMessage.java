package com.gameonline.network.messages;

/**
 * Broadcast by the server while waiting for every connected player to ready up for the next round.
 */
public final class ReplayStatusMessage implements Message {
    private static final long serialVersionUID = 1L;

    private final int readyPlayers;
    private final int totalPlayers;

    public ReplayStatusMessage(int readyPlayers, int totalPlayers) {
        this.readyPlayers = readyPlayers;
        this.totalPlayers = totalPlayers;
    }

    public int getReadyPlayers() {
        return readyPlayers;
    }

    public int getTotalPlayers() {
        return totalPlayers;
    }
}
