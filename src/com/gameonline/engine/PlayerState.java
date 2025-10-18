package com.gameonline.engine;

import com.gameonline.model.Lane;
import com.gameonline.model.PlayerInfo;
import com.gameonline.model.PlayerScore;

/**
 * Mutable representation of a player's runtime state on the server.
 */
public final class PlayerState {
    private final int id;
    private final String name;
    private final Lane lane;
    private int score;
    private int combo;
    private int maxCombo;
    private String lastJudgement = "";

    public PlayerState(int id, String name, Lane lane) {
        this.id = id;
        this.name = name;
        this.lane = lane;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Lane getLane() {
        return lane;
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

    public void applyScore(int deltaScore, boolean resetCombo) {
        if (resetCombo) {
            combo = 0;
        }
        score = Math.max(0, score + deltaScore);
    }

    public void registerJudgement(String judgement, boolean successfulHit) {
        lastJudgement = judgement;
        if (successfulHit) {
            combo++;
            if (combo > maxCombo) {
                maxCombo = combo;
            }
        } else {
            combo = 0;
        }
    }

    public PlayerScore toSnapshot() {
        return new PlayerScore(new PlayerInfo(id, name, lane), score, combo, maxCombo, lastJudgement);
    }

    public void resetForNewSong() {
        score = 0;
        combo = 0;
        maxCombo = 0;
        lastJudgement = "";
    }
}
