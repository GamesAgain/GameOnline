package com.gameonline.network.messages;

/**
 * Broadcast whenever a note is resolved for a player.
 */
public final class HitResultMessage implements Message {
    private static final long serialVersionUID = 1L;

    private final int playerId;
    private final int noteId;
    private final String judgement;
    private final int newScore;
    private final int combo;

    public HitResultMessage(int playerId, int noteId, String judgement, int newScore, int combo) {
        this.playerId = playerId;
        this.noteId = noteId;
        this.judgement = judgement;
        this.newScore = newScore;
        this.combo = combo;
    }

    public int getPlayerId() {
        return playerId;
    }

    public int getNoteId() {
        return noteId;
    }

    public String getJudgement() {
        return judgement;
    }

    public int getNewScore() {
        return newScore;
    }

    public int getCombo() {
        return combo;
    }
}
