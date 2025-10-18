package com.gameonline.model;

import java.io.Serializable;

/**
 * Snapshot of a player's scoring state for broadcasting to clients.
 */
public final class PlayerScore implements Serializable {
    private static final long serialVersionUID = 1L;

    private final PlayerInfo info;
    private final int score;
    private final int combo;
    private final int maxCombo;
    private final String lastJudgement;

    public PlayerScore(PlayerInfo info, int score, int combo, int maxCombo, String lastJudgement) {
        this.info = info;
        this.score = score;
        this.combo = combo;
        this.maxCombo = maxCombo;
        this.lastJudgement = lastJudgement;
    }

    public PlayerInfo getInfo() {
        return info;
    }

    public int getScore() {
        return score;
    }

    public int getCombo() {
        return combo;
    }

    public int getMaxCombo() {
        return maxCombo;
    }

    public String getLastJudgement() {
        return lastJudgement;
    }
}
