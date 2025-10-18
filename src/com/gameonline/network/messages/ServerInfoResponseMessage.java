package com.gameonline.network.messages;

import com.gameonline.model.PlayerInfo;

import java.util.Collections;
import java.util.List;

/**
 * Response payload for a {@link ServerInfoRequestMessage}.
 */
public final class ServerInfoResponseMessage implements Message {
    private static final long serialVersionUID = 1L;

    private final String serverName;
    private final int currentPlayers;
    private final int requiredPlayers;
    private final boolean gameInProgress;
    private final List<PlayerInfo> players;

    public ServerInfoResponseMessage(String serverName, int currentPlayers, int requiredPlayers,
                                     boolean gameInProgress, List<PlayerInfo> players) {
        this.serverName = serverName;
        this.currentPlayers = currentPlayers;
        this.requiredPlayers = requiredPlayers;
        this.gameInProgress = gameInProgress;
        this.players = List.copyOf(players);
    }

    public String getServerName() {
        return serverName;
    }

    public int getCurrentPlayers() {
        return currentPlayers;
    }

    public int getRequiredPlayers() {
        return requiredPlayers;
    }

    public boolean isGameInProgress() {
        return gameInProgress;
    }

    public List<PlayerInfo> getPlayers() {
        return Collections.unmodifiableList(players);
    }
}
