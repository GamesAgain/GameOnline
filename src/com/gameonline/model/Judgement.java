package com.gameonline.model;

/**
 * Possible results when a player attempts to hit a note.
 */
public enum Judgement {
    PERFECT(100, 0.08),
    GREAT(70, 0.14),
    GOOD(50, 0.22),
    MISS(0, 0.35);

    private final int baseScore;
    private final double thresholdSeconds;

    Judgement(int baseScore, double thresholdSeconds) {
        this.baseScore = baseScore;
        this.thresholdSeconds = thresholdSeconds;
    }

    public int getBaseScore() {
        return baseScore;
    }

    public double getThresholdSeconds() {
        return thresholdSeconds;
    }
}
