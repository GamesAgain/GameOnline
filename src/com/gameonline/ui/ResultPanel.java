package com.gameonline.ui;

import com.gameonline.model.PlayerScore;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Comparator;
import java.util.List;

/**
 * Displays the final scoreboard once a match has completed.
 */
public final class ResultPanel extends JPanel {
    private final JPanel resultsContainer = new JPanel();
    private final JButton playAgainButton = new JButton("Play Again");
    private final JLabel statusLabel = new JLabel("", SwingConstants.CENTER);
    private transient Runnable playAgainAction;
    private boolean requestedReplay;

    public ResultPanel() {
        setLayout(new BorderLayout(12, 12));
        setBackground(new Color(18, 18, 28));

        JLabel title = new JLabel("Match Results", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 28f));
        title.setBorder(BorderFactory.createEmptyBorder(12, 12, 6, 12));
        add(title, BorderLayout.NORTH);

        resultsContainer.setLayout(new BoxLayout(resultsContainer, BoxLayout.Y_AXIS));
        resultsContainer.setOpaque(false);
        JScrollPane scrollPane = new JScrollPane(resultsContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(0, 10));
        bottomPanel.setOpaque(false);
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
        statusLabel.setForeground(new Color(210, 210, 210));
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.PLAIN, 15f));
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void displayResults(List<PlayerScore> scores) {
        resultsContainer.removeAll();
        List<PlayerScore> ordered = scores.stream()
                .sorted(Comparator.comparingInt(PlayerScore::getScore).reversed())
                .toList();
        int rank = 1;
        for (PlayerScore score : ordered) {
            if (rank > 1) {
                resultsContainer.add(Box.createVerticalStrut(12));
            }
            resultsContainer.add(createScoreCard(score, rank));
            rank++;
        }
        resultsContainer.add(Box.createVerticalGlue());
        resultsContainer.revalidate();
        resultsContainer.repaint();
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

    private JPanel createScoreCard(PlayerScore score, int rank) {
        JPanel card = new JPanel(new BorderLayout(16, 0));
        card.setOpaque(true);
        Color base = rank == 1 ? new Color(78, 93, 173, 200) : new Color(40, 44, 68, 220);
        card.setBackground(base);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 40), 1, true),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));

        JLabel rankLabel = new JLabel("#" + rank, SwingConstants.CENTER);
        rankLabel.setFont(rankLabel.getFont().deriveFont(Font.BOLD, 28f));
        rankLabel.setForeground(Color.WHITE);
        rankLabel.setPreferredSize(new Dimension(80, 60));
        card.add(rankLabel, BorderLayout.WEST);

        JPanel infoPanel = new JPanel();
        infoPanel.setOpaque(false);
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        JLabel nameLabel = new JLabel(score.getInfo().getName());
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 20f));
        nameLabel.setForeground(Color.WHITE);
        infoPanel.add(nameLabel);

        String comboSummary = String.format("Score: %,d    Combo: %d    Max Combo: %d",
                score.getScore(), score.getCombo(), score.getMaxCombo());
        JLabel comboLabel = new JLabel(comboSummary);
        comboLabel.setFont(comboLabel.getFont().deriveFont(Font.PLAIN, 14f));
        comboLabel.setForeground(new Color(220, 220, 220));
        infoPanel.add(comboLabel);

        if (score.getLastJudgement() != null && !score.getLastJudgement().isBlank()) {
            JLabel judgementLabel = new JLabel("Last judgement: " + score.getLastJudgement());
            judgementLabel.setFont(judgementLabel.getFont().deriveFont(Font.PLAIN, 13f));
            judgementLabel.setForeground(new Color(200, 200, 200));
            infoPanel.add(judgementLabel);
        }

        card.add(infoPanel, BorderLayout.CENTER);

        JLabel scoreLabel = new JLabel(String.format("%,d", score.getScore()), SwingConstants.RIGHT);
        scoreLabel.setFont(scoreLabel.getFont().deriveFont(Font.BOLD, 26f));
        scoreLabel.setForeground(Color.WHITE);
        scoreLabel.setPreferredSize(new Dimension(140, 60));
        card.add(scoreLabel, BorderLayout.EAST);

        return card;
    }
}
