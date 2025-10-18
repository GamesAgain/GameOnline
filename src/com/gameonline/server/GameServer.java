package com.gameonline.server;

import com.gameonline.engine.GameEngine;
import com.gameonline.engine.PlayerInput;
import com.gameonline.engine.PlayerState;
import com.gameonline.model.Lane;
import com.gameonline.model.NoteData;
import com.gameonline.model.PlayerInfo;
import com.gameonline.model.PlayerScore;
import com.gameonline.network.messages.GameOverMessage;
import com.gameonline.network.messages.GameStateUpdateMessage;
import com.gameonline.network.messages.HitResultMessage;
import com.gameonline.network.messages.JoinAcceptedMessage;
import com.gameonline.network.messages.LobbyUpdateMessage;
import com.gameonline.network.messages.Message;
import com.gameonline.network.messages.PlayerLeftMessage;
import com.gameonline.network.messages.ReplayStatusMessage;
import com.gameonline.network.messages.StartGameMessage;
import com.gameonline.util.GameRules;
import com.gameonline.util.NoteChartGenerator;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Authoritative server responsible for managing the game state and synchronising clients.
 */
public final class GameServer {
    private final int port;
    private final int requiredPlayers;

    private final Map<Integer, ClientHandler> clients = new ConcurrentHashMap<>();
    private final Map<Integer, PlayerState> players = new LinkedHashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);
    private volatile boolean gameStarted;
    private GameEngine engine;
    private final Set<Integer> replayReady = ConcurrentHashMap.newKeySet();
    private volatile boolean acceptingReplayVotes;

    public GameServer(int port, int requiredPlayers) {
        this.port = port;
        this.requiredPlayers = Math.max(1, requiredPlayers);
    }

    public void start() throws IOException {
        log("Starting server on port " + port + ". Waiting for " + requiredPlayers + " players...");
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket socket = serverSocket.accept();
                if (gameStarted) {
                    log("Rejecting late connection from " + socket.getRemoteSocketAddress());
                    socket.close();
                    continue;
                }
                try {
                    ClientHandler handler = new ClientHandler(this, socket);
                    handler.start();
                } catch (IOException e) {
                    log("Failed to establish connection: " + e.getMessage());
                    socket.close();
                }
            }
        }
    }

    synchronized int registerPlayer(ClientHandler handler, String playerName) {
        if (gameStarted) {
            throw new IllegalStateException("Game already started");
        }
        int playerId = idGenerator.getAndIncrement();
        Lane lane = assignLane();
        PlayerState state = new PlayerState(playerId, playerName, lane);
        players.put(playerId, state);
        clients.put(playerId, handler);

        List<PlayerInfo> snapshot = lobbySnapshot();
        handler.send(new JoinAcceptedMessage(playerId, lane, snapshot, requiredPlayers));
        broadcast(new LobbyUpdateMessage(snapshot, requiredPlayers));
        log("Player joined: " + playerName + " (lane " + lane + ")");
        if (acceptingReplayVotes) {
            replayReady.add(playerId);
            broadcastReplayStatus();
            if (replayReady.size() >= players.size() && players.size() >= requiredPlayers) {
                startGame();
            }
        } else if (players.size() >= requiredPlayers) {
            startGame();
        }
        return playerId;
    }

    private Lane assignLane() {
        boolean[] used = new boolean[Lane.values().length];
        for (PlayerState state : players.values()) {
            used[state.getLane().getIndex()] = true;
        }
        for (Lane lane : Lane.values()) {
            if (!used[lane.getIndex()]) {
                return lane;
            }
        }
        return Lane.BLUE;
    }

    private void startGame() {
        if (gameStarted) {
            return;
        }
        this.gameStarted = true;
        this.acceptingReplayVotes = false;
        replayReady.clear();
        resetPlayerStates();
        List<NoteData> chart = NoteChartGenerator.generate(GameRules.GAME_DURATION_MS);
        long startTime = System.currentTimeMillis() + GameRules.GAME_COUNTDOWN_MS;
        engine = new GameEngine(new HashMap<>(players), chart, this::broadcastHitResult,
                this::broadcastStateUpdate, this::handleGameFinished, GameRules.GAME_DURATION_MS);
        broadcast(new StartGameMessage(startTime, GameRules.GAME_COUNTDOWN_MS, GameRules.GAME_DURATION_MS, chart));
        engine.start(startTime);
        log("Game starting in " + (GameRules.GAME_COUNTDOWN_MS / 1000) + " seconds");
    }

    void onPlayerInput(PlayerInput input) {
        if (engine != null) {
            engine.enqueueInput(input);
        }
    }

    void onPlayAgainRequest(int playerId) {
        if (!acceptingReplayVotes || !players.containsKey(playerId)) {
            return;
        }
        replayReady.add(playerId);
        broadcastReplayStatus();
        if (replayReady.size() >= players.size() && players.size() >= requiredPlayers) {
            startGame();
        }
    }

    void onClientDisconnected(int playerId) {
        if (playerId <= 0) {
            return;
        }
        clients.remove(playerId);
        PlayerState removed = players.remove(playerId);
        replayReady.remove(playerId);
        if (removed != null) {
            broadcast(new PlayerLeftMessage(playerId));
            broadcast(new LobbyUpdateMessage(lobbySnapshot(), requiredPlayers));
            log("Player disconnected: " + removed.getName());
            broadcastReplayStatus();
        }
    }

    private void broadcast(Message message) {
        for (ClientHandler handler : clients.values()) {
            handler.send(message);
        }
    }

    private void broadcastHitResult(HitResultMessage message) {
        broadcast(message);
    }

    private void broadcastStateUpdate(GameStateUpdateMessage message) {
        broadcast(message);
    }

    private void handleGameFinished(List<PlayerScore> finalScores) {
        broadcast(new GameOverMessage(finalScores));
        log("Game finished");
        engine = null;
        gameStarted = false;
        acceptingReplayVotes = true;
        replayReady.clear();
        broadcastReplayStatus();
    }

    private List<PlayerInfo> lobbySnapshot() {
        List<PlayerInfo> info = new ArrayList<>();
        for (PlayerState state : players.values()) {
            info.add(new PlayerInfo(state.getId(), state.getName(), state.getLane()));
        }
        return new ArrayList<>(info);
    }

    private void broadcastReplayStatus() {
        if (!players.isEmpty()) {
            broadcast(new ReplayStatusMessage(replayReady.size(), players.size()));
        }
    }

    private void resetPlayerStates() {
        for (PlayerState state : players.values()) {
            state.resetForNewSong();
        }
    }

    void log(String message) {
        System.out.println("[Server] " + message);
    }
}
