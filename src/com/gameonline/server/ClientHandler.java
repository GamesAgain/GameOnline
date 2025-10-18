package com.gameonline.server;

import com.gameonline.engine.PlayerInput;
import com.gameonline.network.messages.JoinRequestMessage;
import com.gameonline.network.messages.Message;
import com.gameonline.network.messages.PlayerInputMessage;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Handles the lifecycle of a single network connection on the server.
 */
final class ClientHandler implements Runnable {
    private final GameServer server;
    private final Socket socket;
    private final ObjectOutputStream output;
    private final ObjectInputStream input;
    private volatile boolean running = true;
    private int playerId = -1;

    ClientHandler(GameServer server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        this.output = new ObjectOutputStream(socket.getOutputStream());
        this.output.flush();
        this.input = new ObjectInputStream(socket.getInputStream());
    }

    public void start() {
        Thread thread = new Thread(this, "ClientHandler-" + socket.getRemoteSocketAddress());
        thread.start();
    }

    @Override
    public void run() {
        try {
            handleJoin();
            while (running) {
                Message message = (Message) input.readObject();
                if (message instanceof PlayerInputMessage inputMessage) {
                    server.onPlayerInput(new PlayerInput(inputMessage.getPlayerId(), inputMessage.getPressTimeMillis()));
                }
            }
        } catch (EOFException eof) {
            // Client disconnected gracefully.
        } catch (IOException | ClassNotFoundException ex) {
            server.log("Connection issue: " + ex.getMessage());
        } finally {
            running = false;
            server.onClientDisconnected(this.playerId);
            closeQuietly();
        }
    }

    private void handleJoin() throws IOException, ClassNotFoundException {
        Message first = (Message) input.readObject();
        if (!(first instanceof JoinRequestMessage join)) {
            throw new IOException("Expected JoinRequestMessage but received " + first);
        }
        this.playerId = server.registerPlayer(this, join.getPlayerName());
    }

    void send(Message message) {
        synchronized (output) {
            try {
                output.writeObject(message);
                output.flush();
            } catch (IOException e) {
                server.log("Failed to send message to player " + playerId + ": " + e.getMessage());
            }
        }
    }

    void stopHandler() {
        running = false;
        closeQuietly();
    }

    int getPlayerId() {
        return playerId;
    }

    private void closeQuietly() {
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }
}
