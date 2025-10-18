package com.gameonline.ui;

import com.gameonline.client.GameClient;
import com.gameonline.client.GameClientListener;
import com.gameonline.model.Lane;
import com.gameonline.model.NoteData;
import com.gameonline.model.PlayerInfo;
import com.gameonline.model.PlayerScore;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

/**
 * Swing based frame that renders the lobby, gameplay and result screens.
 */
public final class GameClientFrame extends JFrame implements GameClientListener {
    private final GameClient client;
    private final CardLayout layout = new CardLayout();
    private final JPanel container = new JPanel(layout);
    private final LobbyPanel lobbyPanel = new LobbyPanel();
    private final GameplayPanel gameplayPanel = new GameplayPanel();
    private final ResultPanel resultPanel = new ResultPanel();

    private int playerId;
    private Lane assignedLane;
    private boolean hitKeyPressed;

    public GameClientFrame(GameClient client) {
        super("Rhythm Arena Client");
        this.client = client;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(900, 720));
        setLayout(new BorderLayout());
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                client.close();
            }
        });

        container.add(lobbyPanel, "lobby");
        container.add(gameplayPanel, "game");
        container.add(resultPanel, "result");
        add(container, BorderLayout.CENTER);

        resultPanel.setPlayAgainAction(() -> client.requestReplay());

        pack();
        setLocationRelativeTo(null);
    }

    @Override
    public void onJoined(int playerId, Lane lane, List<PlayerInfo> lobby, int requiredPlayers) {
        this.playerId = playerId;
        this.assignedLane = lane;
        lobbyPanel.updatePlayers(lobby, requiredPlayers);
        layout.show(container, "lobby");
        configureKeyBindings();
        setTitle("Rhythm Arena - Lane " + lane + " (Player " + playerId + ")");
    }

    @Override
    public void onLobbyUpdated(List<PlayerInfo> lobby, int requiredPlayers) {
        lobbyPanel.updatePlayers(lobby, requiredPlayers);
    }

    @Override
    public void onGameStarting(long localStartTimeMillis, long countdownMillis, long songDurationMillis,
                               List<NoteData> chart) {
        gameplayPanel.configureSession(localStartTimeMillis, countdownMillis, songDurationMillis, chart,
                assignedLane, playerId);
        layout.show(container, "game");
    }

    @Override
    public void onHitResult(int playerId, String judgement, int score, int combo) {
        gameplayPanel.registerJudgement(playerId, judgement, score, combo);
    }

    @Override
    public void onGameState(List<PlayerScore> scores) {
        gameplayPanel.updateScores(scores);
    }

    @Override
    public void onGameFinished(List<PlayerScore> finalScores) {
        gameplayPanel.updateScores(finalScores);
        resultPanel.displayResults(finalScores);
        layout.show(container, "result");
    }

    @Override
    public void onReplayStatus(int readyPlayers, int totalPlayers) {
        resultPanel.updateReplayStatus(readyPlayers, totalPlayers);
    }

    @Override
    public void onPlayerLeft(int playerId) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                "Player " + playerId + " disconnected.", "Connection", JOptionPane.WARNING_MESSAGE));
    }

    private void configureKeyBindings() {
        hitKeyPressed = false;
        InputMap inputMap = container.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = container.getActionMap();
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, false), "hit-press");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true), "hit-release");
        actionMap.put("hit-press", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!hitKeyPressed) {
                    hitKeyPressed = true;
                    client.sendLaneHit();
                }
            }
        });
        actionMap.put("hit-release", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hitKeyPressed = false;
            }
        });
    }
}
