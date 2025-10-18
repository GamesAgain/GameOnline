package com.gameonline.ui;

import com.gameonline.model.Lane;
import com.gameonline.model.NoteData;
import com.gameonline.model.PlayerScore;
import com.gameonline.util.GameRules;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Renders the active gameplay view and note lanes.
 */
public final class GameplayPanel extends JPanel {
    private static final int LANE_WIDTH = 160;
    private static final int LANE_GAP = 20;
    private static final int NOTE_HEIGHT = 30;
    private static final int DEAD_ZONE_HEIGHT = 14;

    private final Timer repaintTimer;
    private final Map<Integer, PlayerScore> scores = new HashMap<>();
    private List<NoteData> chart = new ArrayList<>();
    private long startTimeMillis;
    private long countdownMillis;
    private long songDurationMillis;
    private Lane playerLane = Lane.BLUE;
    private int localPlayerId = -1;
    private String judgementMessage = "";
    private long judgementExpireTime;

    public GameplayPanel() {
        setBackground(Color.BLACK);
        repaintTimer = new Timer(16, e -> repaint());
        repaintTimer.start();
    }

    public void configureSession(long startTimeMillis, long countdownMillis, long songDurationMillis,
                                 List<NoteData> chart, Lane playerLane, int localPlayerId) {
        this.startTimeMillis = startTimeMillis;
        this.countdownMillis = countdownMillis;
        this.songDurationMillis = songDurationMillis;
        this.chart = new ArrayList<>(chart);
        this.playerLane = playerLane;
        this.localPlayerId = localPlayerId;
    }

    public void updateScores(List<PlayerScore> scores) {
        this.scores.clear();
        for (PlayerScore score : scores) {
            this.scores.put(score.getInfo().getId(), score);
        }
        repaint();
    }

    public void registerJudgement(int playerId, String judgement, int score, int combo) {
        PlayerScore existing = scores.get(playerId);
        if (existing != null) {
            scores.put(playerId, new PlayerScore(existing.getInfo(), score, combo,
                    Math.max(existing.getMaxCombo(), combo), judgement));
        }
        if (playerId == localPlayerId) {
            this.judgementMessage = judgement + (combo > 0 ? " x" + combo : "");
            this.judgementExpireTime = System.currentTimeMillis() + 1200L;
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int lanes = Lane.values().length;
        int totalWidth = lanes * LANE_WIDTH + (lanes - 1) * LANE_GAP;
        int startX = (width - totalWidth) / 2;
        int topMargin = 60;
        int deadZoneY = height - 140;

        long now = System.currentTimeMillis();
        long elapsed = now - startTimeMillis;

        if (startTimeMillis == 0L) {
            drawCenteredText(g2d, "Waiting for game to start...", width, height);
            return;
        }

        if (elapsed < 0) {
            long countdown = Math.max(0, (-elapsed) / 1000 + 1);
            drawCenteredText(g2d, "Starting in " + countdown, width, height);
        }

        for (Lane lane : Lane.values()) {
            int index = lane.getIndex();
            int x = startX + index * (LANE_WIDTH + LANE_GAP);
            g2d.setColor(new Color(40, 40, 40));
            g2d.fillRoundRect(x, topMargin, LANE_WIDTH, deadZoneY - topMargin + DEAD_ZONE_HEIGHT, 16, 16);
            g2d.setColor(lane.getColor());
            if (lane == playerLane) {
                g2d.setStroke(new BasicStroke(4f));
            } else {
                g2d.setStroke(new BasicStroke(2f));
            }
            g2d.drawRoundRect(x, topMargin, LANE_WIDTH, deadZoneY - topMargin + DEAD_ZONE_HEIGHT, 16, 16);

            g2d.setColor(lane.getColor());
            g2d.fillRect(x, deadZoneY, LANE_WIDTH, DEAD_ZONE_HEIGHT);
        }

        drawNotes(g2d, startX, topMargin, deadZoneY);
        drawHud(g2d, width, height, deadZoneY);
    }

    private void drawNotes(Graphics2D g2d, int startX, int topMargin, int deadZoneY) {
        if (chart == null) {
            return;
        }
        long now = System.currentTimeMillis();
        long elapsed = now - startTimeMillis;
        for (NoteData note : chart) {
            long timeUntilHit = note.getHitTimeMillis() - elapsed;
            if (timeUntilHit > GameRules.NOTE_TRAVEL_TIME_MS) {
                continue;
            }
            if (timeUntilHit < -GameRules.NOTE_TRAVEL_TIME_MS) {
                continue;
            }
            double progress = 1.0 - (double) timeUntilHit / GameRules.NOTE_TRAVEL_TIME_MS;
            progress = Math.min(Math.max(progress, 0.0), 1.2);
            int laneIndex = note.getLane().getIndex();
            int x = startX + laneIndex * (LANE_WIDTH + LANE_GAP);
            int y = (int) (topMargin + progress * (deadZoneY - topMargin));
            g2d.setColor(note.getLane().getColor());
            g2d.fillRoundRect(x + 10, y - NOTE_HEIGHT / 2, LANE_WIDTH - 20, NOTE_HEIGHT, 12, 12);
        }
    }

    private void drawHud(Graphics2D g2d, int width, int height, int deadZoneY) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(getFont().deriveFont(Font.BOLD, 18f));
        int y = 30;
        for (Lane lane : Lane.values()) {
            PlayerScore score = findScoreForLane(lane);
            if (score == null) {
                continue;
            }
            String text = String.format("%s | Score: %d | Combo: %d | Max: %d | %s",
                    score.getInfo().getName(), score.getScore(), score.getCombo(), score.getMaxCombo(),
                    score.getLastJudgement() == null ? "" : score.getLastJudgement());
            g2d.drawString(text, 40, y);
            y += 24;
        }

        if (!judgementMessage.isEmpty()) {
            g2d.setFont(getFont().deriveFont(Font.BOLD, 28f));
            int textWidth = g2d.getFontMetrics().stringWidth(judgementMessage);
            g2d.setColor(Color.WHITE);
            g2d.drawString(judgementMessage, (width - textWidth) / 2, deadZoneY + 60);
            if (System.currentTimeMillis() > judgementExpireTime) {
                judgementMessage = "";
            }
        }

        g2d.setFont(getFont().deriveFont(Font.PLAIN, 16f));
        String keyHint = "Press " + keyForLane(playerLane) + " to hit notes on your lane.";
        int textWidth = g2d.getFontMetrics().stringWidth(keyHint);
        g2d.drawString(keyHint, (width - textWidth) / 2, height - 40);

        if (startTimeMillis > 0L) {
            long remaining = Math.max(0, songDurationMillis - Math.max(0L, System.currentTimeMillis() - startTimeMillis));
            String timeText = String.format("Time left: %02d:%02d", (remaining / 1000) / 60, (remaining / 1000) % 60);
            g2d.drawString(timeText, width - g2d.getFontMetrics().stringWidth(timeText) - 30, height - 40);
        }
    }

    private PlayerScore findScoreForLane(Lane lane) {
        for (PlayerScore score : scores.values()) {
            if (score.getInfo().getLane() == lane) {
                return score;
            }
        }
        return null;
    }

    private String keyForLane(Lane lane) {
        return switch (lane) {
            case BLUE -> "A";
            case YELLOW -> "S";
            case RED -> "D";
        };
    }

    private void drawCenteredText(Graphics2D g2d, String text, int width, int height) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(getFont().deriveFont(Font.BOLD, 32f));
        int textWidth = g2d.getFontMetrics().stringWidth(text);
        int textHeight = g2d.getFontMetrics().getAscent();
        g2d.drawString(text, (width - textWidth) / 2, (height + textHeight) / 2);
    }
}
