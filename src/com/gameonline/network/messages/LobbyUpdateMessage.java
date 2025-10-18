package com.gameonline.network.messages;

import com.gameonline.model.PlayerInfo;

import java.util.List;

/**
 * Broadcast by the server whenever the lobby composition changes.
 */
public final class LobbyUpdateMessage implements Message {
    private static final long serialVersionUID = 1L;

    private final List<PlayerInfo> players;
    private final int requiredPlayers;

    public LobbyUpdateMessage(List<PlayerInfo> players, int requiredPlayers) {
        this.players = players;
        this.requiredPlayers = requiredPlayers;
    }

    public List<PlayerInfo> getPlayers() {
        return players;
    }

    public int getRequiredPlayers() {
        return requiredPlayers;
    }
}
