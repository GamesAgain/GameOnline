package com.gameonline.client;

import com.gameonline.model.Lane;
import com.gameonline.model.NoteData;
import com.gameonline.model.PlayerInfo;
import com.gameonline.model.PlayerScore;
import com.gameonline.network.messages.Message;
import com.gameonline.network.messages.Message.Gameplay.GameOverMessage;
import com.gameonline.network.messages.Message.Gameplay.GameStateUpdateMessage;
import com.gameonline.network.messages.Message.Gameplay.HitResultMessage;
import com.gameonline.network.messages.Message.Gameplay.PlayerInputMessage;
import com.gameonline.network.messages.Message.Gameplay.StartGameMessage;
import com.gameonline.network.messages.Message.Lobby.JoinAcceptedMessage;
import com.gameonline.network.messages.Message.Lobby.JoinRequestMessage;
import com.gameonline.network.messages.Message.Lobby.LobbyUpdateMessage;
import com.gameonline.network.messages.Message.Lobby.PlayerLeftMessage;
import com.gameonline.network.messages.Message.PostGame.PlayAgainRequestMessage;
import com.gameonline.network.messages.Message.PostGame.ReplayStatusMessage;

import javax.swing.SwingUtilities;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Client responsible for communicating with the authoritative server and forwarding
 * relevant events to the UI layer.
 */
public final class GameClient implements AutoCloseable {
    private final String host;
    private final int port;
    private final String playerName;
    private volatile GameClientListener listener;

    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "GameClient-Receiver");
        t.setDaemon(true);
        return t;
    });

    private volatile boolean running;
    private int playerId;
    private Lane assignedLane;
    private long timeOffsetMillis;

    public GameClient(String host, int port, String playerName, GameClientListener listener) {
        this.host = host;
        this.port = port;
        this.playerName = playerName;
        this.listener = listener;
    }

    public void connect() throws IOException {
        this.socket = new Socket(host, port);
        this.output = new ObjectOutputStream(socket.getOutputStream());
        this.output.flush();
        this.input = new ObjectInputStream(socket.getInputStream());
        send(new JoinRequestMessage(playerName));
        running = true;
        executor.submit(this::receiveLoop);
    }

    private void receiveLoop() {
        try {
            while (running) {
                Message message = (Message) input.readObject();
                handleMessage(message);
            }
        } catch (IOException | ClassNotFoundException e) {
            if (running) {
                System.err.println("[Client] Connection closed: " + e.getMessage());
            }
        } finally {
            running = false;
            closeQuietly();
        }
    }

    private void handleMessage(Message message) {
        if (message instanceof JoinAcceptedMessage accepted) {
            this.playerId = accepted.getPlayerId();
            this.assignedLane = accepted.getAssignedLane();
            GameClientListener current = listener;
            if (current != null) {
                SwingUtilities.invokeLater(() -> current.onJoined(playerId, assignedLane,
                        accepted.getLobbyPlayers(), accepted.getRequiredPlayers()));
            }
        } else if (message instanceof LobbyUpdateMessage lobby) {
            GameClientListener current = listener;
            if (current != null) {
                SwingUtilities.invokeLater(() -> current.onLobbyUpdated(lobby.getPlayers(), lobby.getRequiredPlayers()));
            }
        } else if (message instanceof StartGameMessage start) {
            long now = System.currentTimeMillis();
            timeOffsetMillis = start.getServerStartTimeMillis() - now;
            long localStartTime = now + timeOffsetMillis;
            GameClientListener current = listener;
            if (current != null) {
                SwingUtilities.invokeLater(() -> current.onGameStarting(localStartTime,
                        start.getCountdownMillis(), start.getSongDurationMillis(), start.getChart()));
            }
        } else if (message instanceof HitResultMessage result) {
            GameClientListener current = listener;
            if (current != null) {
                SwingUtilities.invokeLater(() -> current.onHitResult(result.getPlayerId(), result.getJudgement(),
                        result.getNewScore(), result.getCombo()));
            }
        } else if (message instanceof GameStateUpdateMessage update) {
            GameClientListener current = listener;
            if (current != null) {
                SwingUtilities.invokeLater(() -> current.onGameState(update.getScores()));
            }
        } else if (message instanceof GameOverMessage over) {
            GameClientListener current = listener;
            if (current != null) {
                SwingUtilities.invokeLater(() -> current.onGameFinished(over.getFinalScores()));
            }
        } else if (message instanceof ReplayStatusMessage replay) {
            GameClientListener current = listener;
            if (current != null) {
                SwingUtilities.invokeLater(() -> current.onReplayStatus(replay.getReadyPlayers(),
                        replay.getTotalPlayers()));
            }
        } else if (message instanceof PlayerLeftMessage left) {
            GameClientListener current = listener;
            if (current != null) {
                SwingUtilities.invokeLater(() -> current.onPlayerLeft(left.getPlayerId()));
            }
        }
    }

    public void sendLaneHit() {
        if (!running) {
            return;
        }
        long serverTimeEstimate = System.currentTimeMillis() + timeOffsetMillis;
        send(new PlayerInputMessage(playerId, serverTimeEstimate));
    }

    public void requestReplay() {
        if (!running) {
            return;
        }
        send(new PlayAgainRequestMessage());
    }

    private void send(Message message) {
        synchronized (output) {
            try {
                output.writeObject(message);
                output.flush();
            } catch (IOException e) {
                System.err.println("[Client] Failed to send message: " + e.getMessage());
            }
        }
    }

    public int getPlayerId() {
        return playerId;
    }

    public Lane getAssignedLane() {
        return assignedLane;
    }

    public void setListener(GameClientListener listener) {
        this.listener = listener;
    }

    @Override
    public void close() {
        running = false;
        executor.shutdownNow();
        closeQuietly();
    }

    private void closeQuietly() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ignored) {
        }
    }
}
