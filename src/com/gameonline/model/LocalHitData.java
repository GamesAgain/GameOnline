package com.gameonline.model;

/**
 * Encapsulates the locally evaluated hit information before it is synchronised with the server.
 */
public final class LocalHitData {
    public static final int NO_NOTE_ID = -1;
    public static final long INVALID_DELTA = Long.MIN_VALUE;

    private final int noteId;
    private final long clientPressTimeMillis;
    private final long deltaMillis;
    private final Judgement predictedJudgement;

    public LocalHitData(int noteId, long clientPressTimeMillis, long deltaMillis, Judgement predictedJudgement) {
        this.noteId = noteId;
        this.clientPressTimeMillis = clientPressTimeMillis;
        this.deltaMillis = deltaMillis;
        this.predictedJudgement = predictedJudgement;
    }

    public static LocalHitData miss(long clientPressTimeMillis) {
        return new LocalHitData(NO_NOTE_ID, clientPressTimeMillis, INVALID_DELTA, Judgement.MISS);
    }

    public int getNoteId() {
        return noteId;
    }

    public long getClientPressTimeMillis() {
        return clientPressTimeMillis;
    }

    public long getDeltaMillis() {
        return deltaMillis;
    }

    public Judgement getPredictedJudgement() {
        return predictedJudgement;
    }

    public boolean hasValidNote() {
        return noteId != NO_NOTE_ID;
    }

    public boolean hasValidDelta() {
        return deltaMillis != INVALID_DELTA;
    }
}
