package com.gameonline.client;

import com.gameonline.model.PlayerInfo;

import java.net.InetAddress;
import java.util.List;
import java.util.Objects;

/**
 * Immutable snapshot describing a discovered server instance.
 */
public final class DiscoveredServer {
    private final InetAddress address;
    private final int port;
    private final String serverName;
    private final int currentPlayers;
    private final int requiredPlayers;
    private final boolean gameInProgress;
    private final List<PlayerInfo> players;

    public DiscoveredServer(InetAddress address, int port, String serverName, int currentPlayers,
                            int requiredPlayers, boolean gameInProgress, List<PlayerInfo> players) {
        this.address = Objects.requireNonNull(address, "address");
        this.port = port;
        this.serverName = Objects.requireNonNull(serverName, "serverName");
        this.currentPlayers = currentPlayers;
        this.requiredPlayers = requiredPlayers;
        this.gameInProgress = gameInProgress;
        this.players = List.copyOf(players);
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
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
        return players;
    }

    public String getDisplayAddress() {
        return address.getHostAddress() + ":" + port;
    }

    public String getStatusText() {
        String base = currentPlayers + "/" + requiredPlayers + " players";
        if (gameInProgress) {
            return base + " (In Game)";
        }
        return base + " (Lobby)";
    }

    @Override
    public String toString() {
        return serverName + " - " + getDisplayAddress() + " - " + getStatusText();
    }
}
