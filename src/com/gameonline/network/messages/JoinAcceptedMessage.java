package com.gameonline.network.messages;

import com.gameonline.model.Lane;
import com.gameonline.model.PlayerInfo;

import java.util.List;

/**
 * Sent by the server to confirm a player has joined and provide lobby context.
 */
public final class JoinAcceptedMessage implements Message {
    private static final long serialVersionUID = 1L;

    private final int playerId;
    private final Lane assignedLane;
    private final List<PlayerInfo> lobbyPlayers;
    private final int requiredPlayers;

    public JoinAcceptedMessage(int playerId, Lane assignedLane, List<PlayerInfo> lobbyPlayers, int requiredPlayers) {
        this.playerId = playerId;
        this.assignedLane = assignedLane;
        this.lobbyPlayers = lobbyPlayers;
        this.requiredPlayers = requiredPlayers;
    }

    public int getPlayerId() {
        return playerId;
    }

    public Lane getAssignedLane() {
        return assignedLane;
    }

    public List<PlayerInfo> getLobbyPlayers() {
        return lobbyPlayers;
    }

    public int getRequiredPlayers() {
        return requiredPlayers;
    }
}
