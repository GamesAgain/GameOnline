package com.gameonline.ui;

import com.gameonline.model.Lane;
import com.gameonline.model.NoteData;
import com.gameonline.model.PlayerScore;
import com.gameonline.util.GameRules;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.io.IOException;
import java.net.URL;
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
    private static final int NOTE_HEIGHT = 36;
    private static final int DEAD_ZONE_HEIGHT = 36;
    private static final int HIT_GUIDE_LINE_THICKNESS = 4;
    private static final Color LANE_BACKGROUND = new Color(0, 0, 0, 160);
    private static final Color PLAYER_LANE_OVERLAY = new Color(255, 255, 255, 40);
    private static final Color OTHER_LANE_OVERLAY = new Color(255, 255, 255, 18);

    private final Timer repaintTimer;
    private final Map<Integer, PlayerScore> scores = new HashMap<>();
    private final Image backgroundImage;
    private List<NoteData> chart = new ArrayList<>();
    private long startTimeMillis;
    private long countdownMillis;
    private long songDurationMillis;
    private Lane playerLane = Lane.BLUE;
    private int localPlayerId = -1;
    private String localPlayerName = "";
    private String judgementMessage = "";
    private long judgementExpireTime;

    public GameplayPanel() {
        setBackground(Color.BLACK);
        backgroundImage = loadBackgroundImage();
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
            if (score.getInfo().getId() == localPlayerId) {
                this.localPlayerName = score.getInfo().getName();
            }
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

        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, 0, width, height, this);
        }
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
            g2d.setColor(LANE_BACKGROUND);
            g2d.fillRoundRect(x, topMargin, LANE_WIDTH, deadZoneY - topMargin + DEAD_ZONE_HEIGHT, 16, 16);
            g2d.setColor(lane == playerLane ? PLAYER_LANE_OVERLAY : OTHER_LANE_OVERLAY);
            g2d.fillRoundRect(x, topMargin, LANE_WIDTH, deadZoneY - topMargin + DEAD_ZONE_HEIGHT, 16, 16);
            g2d.setColor(lane.getColor());
            if (lane == playerLane) {
                g2d.setStroke(new BasicStroke(4f));
            } else {
                g2d.setStroke(new BasicStroke(2f));
            }
            g2d.drawRoundRect(x, topMargin, LANE_WIDTH, deadZoneY - topMargin + DEAD_ZONE_HEIGHT, 16, 16);

            drawTimingGuide(g2d, lane, x, deadZoneY);
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
            g2d.fillRoundRect(x + 12, y - NOTE_HEIGHT / 2, LANE_WIDTH - 24, NOTE_HEIGHT, 16, 16);
        }
    }

    private void drawHud(Graphics2D g2d, int width, int height, int deadZoneY) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(getFont().deriveFont(Font.BOLD, 20f));
        String bannerText = localPlayerName.isEmpty()
                ? "You are playing lane " + playerLane.name()
                : localPlayerName + " - lane " + playerLane.name();
        int bannerWidth = g2d.getFontMetrics().stringWidth(bannerText) + 24;
        int bannerHeight = g2d.getFontMetrics().getHeight() + 12;
        int bannerX = Math.max(20, (width - bannerWidth) / 2);
        g2d.setColor(new Color(0, 0, 0, 160));
        g2d.fillRoundRect(bannerX, 16, bannerWidth, bannerHeight, 20, 20);
        g2d.setColor(Color.WHITE);
        g2d.drawString(bannerText, bannerX + 12, 16 + g2d.getFontMetrics().getAscent() + 2);

        g2d.setFont(getFont().deriveFont(Font.BOLD, 18f));
        int y = 60 + bannerHeight;
        for (Lane lane : Lane.values()) {
            PlayerScore score = findScoreForLane(lane);
            if (score == null) {
                continue;
            }
            String text = String.format("%s | Score: %d | Combo: %d | Max: %d | %s",
                    score.getInfo().getName(), score.getScore(), score.getCombo(), score.getMaxCombo(),
                    score.getLastJudgement() == null ? "" : score.getLastJudgement());
            int textWidth = g2d.getFontMetrics().stringWidth(text);
            int textX = 40;
            if (lane == playerLane) {
                g2d.setColor(new Color(255, 255, 255, 40));
                g2d.fillRoundRect(textX - 16, y - g2d.getFontMetrics().getAscent(), textWidth + 32,
                        g2d.getFontMetrics().getHeight() + 6, 18, 18);
                g2d.setColor(Color.WHITE);
            } else {
                g2d.setColor(new Color(220, 220, 220));
            }
            g2d.drawString(text, textX, y);
            y += 28;
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
        String keyHint = "Press " + keyForLane() + " to hit notes on your lane.";
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

    private String keyForLane() {
        return "SPACEBAR";
    }

    private void drawTimingGuide(Graphics2D g2d, Lane lane, int x, int targetY) {
        int guideTop = targetY - DEAD_ZONE_HEIGHT / 2;
        int guideHeight = DEAD_ZONE_HEIGHT;
        Color laneColor = lane.getColor();
        Color fillColor = new Color(laneColor.getRed(), laneColor.getGreen(), laneColor.getBlue(),
                lane == playerLane ? 160 : 120);
        g2d.setColor(fillColor);
        g2d.fillRoundRect(x + 8, guideTop, LANE_WIDTH - 16, guideHeight, 18, 18);

        g2d.setStroke(new BasicStroke(HIT_GUIDE_LINE_THICKNESS));
        g2d.setColor(Color.WHITE);
        g2d.drawLine(x + 12, targetY, x + LANE_WIDTH - 12, targetY);

        if (lane == playerLane) {
            g2d.setFont(getFont().deriveFont(Font.BOLD, 16f));
            String hint = "HIT!";
            int hintWidth = g2d.getFontMetrics().stringWidth(hint);
            g2d.drawString(hint, x + (LANE_WIDTH - hintWidth) / 2, guideTop - 8);
        }
        g2d.setStroke(new BasicStroke(1f));
    }

    private void drawCenteredText(Graphics2D g2d, String text, int width, int height) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(getFont().deriveFont(Font.BOLD, 32f));
        int textWidth = g2d.getFontMetrics().stringWidth(text);
        int textHeight = g2d.getFontMetrics().getAscent();
        g2d.drawString(text, (width - textWidth) / 2, (height + textHeight) / 2);
    }

    private Image loadBackgroundImage() {
        URL resource = getClass().getResource("/com/gameonline/assets/images/bg.png");
        if (resource == null) {
            return null;
        }
        try {
            return ImageIO.read(resource);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
