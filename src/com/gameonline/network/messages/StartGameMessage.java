package com.gameonline.network.messages;

import com.gameonline.model.NoteData;

import java.util.List;

/**
 * Informs clients that the game is starting imminently.
 */
public final class StartGameMessage implements Message {
    private static final long serialVersionUID = 1L;

    private final long serverStartTimeMillis;
    private final long countdownMillis;
    private final long songDurationMillis;
    private final List<NoteData> chart;

    public StartGameMessage(long serverStartTimeMillis, long countdownMillis, long songDurationMillis,
                            List<NoteData> chart) {
        this.serverStartTimeMillis = serverStartTimeMillis;
        this.countdownMillis = countdownMillis;
        this.songDurationMillis = songDurationMillis;
        this.chart = chart;
    }

    public long getServerStartTimeMillis() {
        return serverStartTimeMillis;
    }

    public long getCountdownMillis() {
        return countdownMillis;
    }

    public long getSongDurationMillis() {
        return songDurationMillis;
    }

    public List<NoteData> getChart() {
        return chart;
    }
}
