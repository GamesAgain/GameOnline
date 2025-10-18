package com.gameonline.network.messages;

/**
 * Sent by a client immediately after connecting to announce itself.
 */
public final class JoinRequestMessage implements Message {
    private static final long serialVersionUID = 1L;

    private final String playerName;

    public JoinRequestMessage(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerName() {
        return playerName;
    }
}
