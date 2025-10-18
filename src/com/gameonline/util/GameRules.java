package com.gameonline.util;

import com.gameonline.model.Judgement;

/**
 * Centralises configurable gameplay constants.
 */
public final class GameRules {
    private GameRules() {
    }

    public static final long NOTE_TRAVEL_TIME_MS = 2000L;
    public static final long GAME_DURATION_MS = 60000L;
    public static final long GAME_COUNTDOWN_MS = 3000L;
    public static final long MISS_WINDOW_MS = 420L;

    public static int calculateScore(Judgement judgement, int combo) {
        int base = judgement.getBaseScore();
        if (judgement == Judgement.MISS) {
            return 0;
        }
        int comboBonus = Math.max(0, combo - 1) * 5;
        return base + comboBonus;
    }

    public static Judgement judgementForDelta(long deltaMillis) {
        double deltaSeconds = Math.abs(deltaMillis) / 1000.0;
        if (deltaSeconds <= Judgement.PERFECT.getThresholdSeconds()) {
            return Judgement.PERFECT;
        }
        if (deltaSeconds <= Judgement.GREAT.getThresholdSeconds()) {
            return Judgement.GREAT;
        }
        if (deltaSeconds <= Judgement.GOOD.getThresholdSeconds()) {
            return Judgement.GOOD;
        }
        if (deltaSeconds <= Judgement.MISS.getThresholdSeconds()) {
            return Judgement.MISS;
        }
        return Judgement.MISS;
    }
}
