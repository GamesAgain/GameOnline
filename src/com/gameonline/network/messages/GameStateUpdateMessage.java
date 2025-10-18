package com.gameonline.network.messages;

import com.gameonline.model.PlayerScore;

import java.util.List;

/**
 * Periodically broadcast scoreboard information from the server.
 */
public final class GameStateUpdateMessage implements Message {
    private static final long serialVersionUID = 1L;

    private final List<PlayerScore> scores;

    public GameStateUpdateMessage(List<PlayerScore> scores) {
        this.scores = scores;
    }

    public List<PlayerScore> getScores() {
        return scores;
    }
}
