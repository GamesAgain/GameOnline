package com.gameonline.ui;

import com.gameonline.model.PlayerScore;

import javax.swing.BorderFactory;
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

    public ResultPanel() {
        setLayout(new BorderLayout(8, 8));
        JLabel title = new JLabel("Results", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 26f));
        add(title, BorderLayout.NORTH);

        resultsArea.setEditable(false);
        resultsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
        resultsArea.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        add(new JScrollPane(resultsArea), BorderLayout.CENTER);
    }

    public void displayResults(List<PlayerScore> scores) {
        resultsArea.setText("");
        scores.stream()
                .sorted(Comparator.comparingInt(PlayerScore::getScore).reversed())
                .forEach(score -> resultsArea.append(String.format("%s\n  Score: %d\n  Max Combo: %d\n\n",
                        score.getInfo().getName(), score.getScore(), score.getMaxCombo())));
    }
}
