package com.gameonline.ui;

import com.gameonline.model.PlayerInfo;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Font;
import java.util.List;

/**
 * Displays the pre-game lobby state.
 */
public final class LobbyPanel extends JPanel {
    private final DefaultListModel<String> model = new DefaultListModel<>();
    private final JLabel statusLabel = new JLabel("", SwingConstants.CENTER);

    public LobbyPanel() {
        setLayout(new BorderLayout(8, 8));
        JLabel title = new JLabel("Rhythm Arena Lobby", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        add(title, BorderLayout.NORTH);

        JList<String> list = new JList<>(model);
        list.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        add(new JScrollPane(list), BorderLayout.CENTER);

        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.PLAIN, 16f));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        add(statusLabel, BorderLayout.SOUTH);
    }

    public void updatePlayers(List<PlayerInfo> players, int required) {
        model.clear();
        for (PlayerInfo info : players) {
            model.addElement(info.getName() + " - " + info.getLane());
        }
        statusLabel.setText("Waiting for players: " + players.size() + "/" + required);
    }
}
