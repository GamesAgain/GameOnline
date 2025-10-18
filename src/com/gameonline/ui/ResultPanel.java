package com.gameonline.ui;

import com.gameonline.model.PlayerScore;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Font;
import java.util.Comparator;
import java.util.List;

/**
 * Displays the final scoreboard once a match has completed.
 */
public final class ResultPanel extends JPanel {
    private final JTextArea resultsArea = new JTextArea();
    private final JButton playAgainButton = new JButton("Play Again");
    private final JLabel statusLabel = new JLabel("", SwingConstants.CENTER);
    private transient Runnable playAgainAction;
    private boolean requestedReplay;

    public ResultPanel() {
        setLayout(new BorderLayout(8, 8));
        JLabel title = new JLabel("Results", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 26f));
        add(title, BorderLayout.NORTH);

        resultsArea.setEditable(false);
        resultsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
        resultsArea.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        add(new JScrollPane(resultsArea), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(0, 6));
        playAgainButton.setFont(playAgainButton.getFont().deriveFont(Font.BOLD, 16f));
        playAgainButton.addActionListener(e -> {
            requestedReplay = true;
            playAgainButton.setEnabled(false);
            statusLabel.setText("Ready! Waiting for other players...");
            if (playAgainAction != null) {
                playAgainAction.run();
            }
        });
        bottomPanel.add(playAgainButton, BorderLayout.CENTER);

        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 4, 12, 4));
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.PLAIN, 14f));
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void displayResults(List<PlayerScore> scores) {
        resultsArea.setText("");
        scores.stream()
                .sorted(Comparator.comparingInt(PlayerScore::getScore).reversed())
                .forEach(score -> resultsArea.append(String.format("%s\n  Score: %d\n  Max Combo: %d\n\n",
                        score.getInfo().getName(), score.getScore(), score.getMaxCombo())));
        requestedReplay = false;
        playAgainButton.setEnabled(true);
        statusLabel.setText("Press Play Again when you're ready to restart.");
    }

    public void setPlayAgainAction(Runnable playAgainAction) {
        this.playAgainAction = playAgainAction;
    }

    public void updateReplayStatus(int readyPlayers, int totalPlayers) {
        if (totalPlayers <= 0) {
            statusLabel.setText("Waiting for players...");
            return;
        }
        String text = String.format("Ready players: %d/%d", readyPlayers, totalPlayers);
        if (requestedReplay && readyPlayers >= totalPlayers) {
            text = "All players ready! Starting...";
        }
        statusLabel.setText(text);
    }
}
