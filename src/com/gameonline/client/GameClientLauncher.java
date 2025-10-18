package com.gameonline.client;

import com.gameonline.ui.GameClientFrame;
import com.gameonline.ui.ServerBrowserFrame;

import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;
import java.io.IOException;

/**
 * Bootstraps the Swing UI and establishes a client connection.
 */
public final class GameClientLauncher {
    private GameClientLauncher() {
    }

    public static void launch(String host, int port, String playerName) {
        if (host == null || host.isBlank()) {
            launchServerBrowser(port, playerName);
            return;
        }
        launch(host, port, playerName, null);
    }

    public static void launch(String host, int port, String playerName, Runnable onConnectionFailure) {
        String resolvedName = resolvePlayerName(playerName);
        SwingUtilities.invokeLater(() -> {
            GameClient client = new GameClient(host, port, resolvedName, null);
            GameClientFrame frame = new GameClientFrame(client);
            client.setListener(frame);
            frame.setVisible(true);

            Thread connectionThread = new Thread(() -> {
                try {
                    client.connect();
                } catch (IOException e) {
                    client.close();
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(frame,
                                "Failed to connect: " + e.getMessage(), "Connection Error",
                                JOptionPane.ERROR_MESSAGE);
                        frame.dispose();
                        if (onConnectionFailure != null) {
                            onConnectionFailure.run();
                        }
                    });
                }
            }, "Client-Connector");
            connectionThread.setDaemon(true);
            connectionThread.start();
        });
    }

    public static void launchServerBrowser(int port, String playerName) {
        String resolvedName = resolvePlayerName(playerName);
        SwingUtilities.invokeLater(() -> {
            ServerBrowserFrame frame = new ServerBrowserFrame(port, resolvedName);
            frame.setVisible(true);
        });
    }

    private static String resolvePlayerName(String playerName) {
        if (playerName == null || playerName.isBlank()) {
            return "Player" + (int) (Math.random() * 1000);
        }
        return playerName;
    }
}
