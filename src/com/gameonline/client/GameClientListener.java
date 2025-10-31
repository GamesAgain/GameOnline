package com.gameonline.client;

import com.gameonline.model.Lane;
import com.gameonline.model.NoteData;
import com.gameonline.model.PlayerInfo;
import com.gameonline.model.PlayerScore;

import java.util.List;

/**
 * Callback interface used by the networking layer to inform the UI of state changes.
 */
public interface GameClientListener {
    void onJoined(int playerId, Lane lane, List<PlayerInfo> lobby, int requiredPlayers);

    void onLobbyUpdated(List<PlayerInfo> lobby, int requiredPlayers);

    void onGameStarting(long localStartTimeMillis, long countdownMillis, long songDurationMillis,
                        List<NoteData> chart);

    void onHitResult(int playerId, int noteId, String judgement, int score, int combo);

    void onGameState(List<PlayerScore> scores);

    void onGameFinished(List<PlayerScore> finalScores);

    void onPlayerLeft(int playerId);

    void onReplayStatus(int readyPlayers, int totalPlayers);
}
