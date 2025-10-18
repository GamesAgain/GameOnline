package com.gameonline.network.messages;

import com.gameonline.model.PlayerScore;

import java.util.List;

/**
 * Indicates the match has finished.
 */
public final class GameOverMessage implements Message {
    private static final long serialVersionUID = 1L;

    private final List<PlayerScore> finalScores;

    public GameOverMessage(List<PlayerScore> finalScores) {
        this.finalScores = finalScores;
    }

    public List<PlayerScore> getFinalScores() {
        return finalScores;
    }
}
