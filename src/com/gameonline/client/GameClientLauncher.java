package com.gameonline.client;

import com.gameonline.ui.GameClientFrame;

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
        SwingUtilities.invokeLater(() -> {
            GameClient client = new GameClient(host, port, playerName, null);
            GameClientFrame frame = new GameClientFrame(client);
            client.setListener(frame);
            frame.setVisible(true);

            Thread connectionThread = new Thread(() -> {
                try {
                    client.connect();
                } catch (IOException e) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame,
                            "Failed to connect: " + e.getMessage(), "Connection Error",
                            JOptionPane.ERROR_MESSAGE));
                }
            }, "Client-Connector");
            connectionThread.setDaemon(true);
            connectionThread.start();
        });
    }
}
