package com.gameonline.ui;

import com.gameonline.client.DiscoveredServer;
import com.gameonline.client.ServerDiscovery;
import com.gameonline.client.GameClientLauncher;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;

/**
 * Simple lobby window that allows players to browse and join LAN servers.
 */
public final class ServerBrowserFrame extends JFrame {
    private final DefaultListModel<DiscoveredServer> serverListModel = new DefaultListModel<>();
    private final JList<DiscoveredServer> serverList = new JList<>(serverListModel);
    private final JLabel statusLabel = new JLabel("", SwingConstants.LEFT);
    private final JButton refreshButton = new JButton("Refresh");
    private final JButton joinSelectedButton = new JButton("Join Selected");
    private final JButton manualJoinButton = new JButton("Join via Address");
    private final JTextField nameField = new JTextField();
    private final JTextField manualHostField = new JTextField();
    private final JSpinner portSpinner;

    private final int defaultPort;

    public ServerBrowserFrame(int defaultPort, String defaultPlayerName) {
        super("Rhythm Arena - Server Browser");
        this.defaultPort = defaultPort;
        this.portSpinner = new JSpinner(new SpinnerNumberModel(defaultPort, 1, 65535, 1));
        this.nameField.setText(defaultPlayerName);
        configureUi();
        attachListeners();
        setPreferredSize(new Dimension(640, 520));
        pack();
        setLocationRelativeTo(null);
        startDiscovery();
    }

    private void configureUi() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setContentPane(root);

        JLabel title = new JLabel("Find a server", SwingConstants.LEFT);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));

        JLabel subtitle = new JLabel("Choose a server below or enter an address manually.");
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 14f));

        JPanel header = new JPanel(new BorderLayout(8, 8));
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.CENTER);

        JPanel namePanel = new JPanel(new BorderLayout(8, 8));
        JLabel nameLabel = new JLabel("Player name:");
        nameField.setColumns(16);
        namePanel.add(nameLabel, BorderLayout.WEST);
        namePanel.add(nameField, BorderLayout.CENTER);
        header.add(namePanel, BorderLayout.SOUTH);
        root.add(header, BorderLayout.NORTH);

        serverList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        serverList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel();
            label.setOpaque(true);
            String text = String.format("<html><b>%s</b><br>%s<br><span style='color:#888;'>%s</span></html>",
                    value.getServerName(), value.getDisplayAddress(), value.getStatusText());
            label.setText(text);
            label.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
            if (!value.getPlayers().isEmpty()) {
                String tooltip = value.getPlayers().stream()
                        .map(player -> player.getName() + " (" + player.getLane() + ")")
                        .collect(java.util.stream.Collectors.joining(", "));
                label.setToolTipText("Players: " + tooltip);
            } else {
                label.setToolTipText("No players connected yet.");
            }
            if (isSelected) {
                label.setBackground(list.getSelectionBackground());
                label.setForeground(list.getSelectionForeground());
            } else {
                label.setBackground(list.getBackground());
                label.setForeground(list.getForeground());
            }
            return label;
        });
        JScrollPane scrollPane = new JScrollPane(serverList);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(scrollPane.getForeground().darker()),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)));
        root.add(scrollPane, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.PLAIN, 14f));
        bottom.add(statusLabel, BorderLayout.NORTH);

        JPanel controls = new JPanel();
        controls.add(refreshButton);
        controls.add(joinSelectedButton);
        joinSelectedButton.setEnabled(false);
        bottom.add(controls, BorderLayout.CENTER);

        JPanel manualPanel = new JPanel();
        manualPanel.setBorder(BorderFactory.createTitledBorder("Manual join"));
        manualHostField.setColumns(12);
        manualPanel.add(new JLabel("Address:"));
        manualPanel.add(manualHostField);
        manualPanel.add(new JLabel(":"));
        portSpinner.setEditor(new JSpinner.NumberEditor(portSpinner, "#####"));
        JFormattedTextField portField = ((JSpinner.NumberEditor) portSpinner.getEditor()).getTextField();
        portField.setColumns(5);
        manualPanel.add(portSpinner);
        manualPanel.add(manualJoinButton);
        bottom.add(manualPanel, BorderLayout.SOUTH);

        root.add(bottom, BorderLayout.SOUTH);
    }

    private void attachListeners() {
        refreshButton.addActionListener(e -> startDiscovery());
        serverList.addListSelectionListener(e -> joinSelectedButton.setEnabled(!serverList.isSelectionEmpty()));
        joinSelectedButton.addActionListener(e -> joinSelectedServer());
        manualJoinButton.addActionListener(e -> joinManualAddress());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void startDiscovery() {
        refreshButton.setEnabled(false);
        joinSelectedButton.setEnabled(false);
        serverListModel.clear();
        statusLabel.setText("Scanning local network...");
        SwingWorker<List<DiscoveredServer>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<DiscoveredServer> doInBackground() {
                return ServerDiscovery.discover(defaultPort);
            }

            @Override
            protected void done() {
                try {
                    List<DiscoveredServer> servers = get();
                    if (servers.isEmpty()) {
                        statusLabel.setText("No servers found. You can still join manually below.");
                    } else {
                        statusLabel.setText("Select a server to join.");
                        servers.forEach(serverListModel::addElement);
                    }
                } catch (Exception ex) {
                    statusLabel.setText("Failed to scan for servers: " + ex.getMessage());
                } finally {
                    refreshButton.setEnabled(true);
                    joinSelectedButton.setEnabled(!serverListModel.isEmpty());
                }
            }
        };
        worker.execute();
    }

    private void joinSelectedServer() {
        DiscoveredServer selected = serverList.getSelectedValue();
        if (selected == null) {
            return;
        }
        joinServer(selected.getAddress().getHostAddress(), selected.getPort());
    }

    private void joinManualAddress() {
        String host = manualHostField.getText().trim();
        int port = (Integer) portSpinner.getValue();
        if (host.isEmpty()) {
            statusLabel.setText("Please enter a valid address.");
            return;
        }
        joinServer(host, port);
    }

    private void joinServer(String host, int port) {
        String playerName = nameField.getText().trim();
        if (playerName.isEmpty()) {
            statusLabel.setText("Please enter a player name.");
            nameField.requestFocusInWindow();
            return;
        }
        dispose();
        SwingUtilities.invokeLater(() -> GameClientLauncher.launch(host, port, playerName,
                () -> GameClientLauncher.launchServerBrowser(defaultPort, playerName)));
    }
}
