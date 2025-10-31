package com.gameonline.network.messages;

import com.gameonline.model.Lane;
import com.gameonline.model.NoteData;
import com.gameonline.model.PlayerInfo;
import com.gameonline.model.PlayerScore;

import java.io.Serializable;
import java.util.List;

/**
 * Base marker interface for serialised messages exchanged between server and clients.
 *
 * <p>To keep the networking package compact while still conveying intent, message
 * implementations are grouped into nested categories reflecting the different phases of
 * gameplay.</p>
 */
public interface Message extends Serializable {

    // ------------------------------------------------------------------
    // Discovery
    // ------------------------------------------------------------------

    final class Discovery {
        private Discovery() {
        }

        /**
         * Lightweight ping message that allows clients to discover servers on the LAN without
         * committing to a full join handshake.
         */
        public static final class ServerInfoRequestMessage implements Message {
            private static final long serialVersionUID = 1L;

            @Override
            public String toString() {
                return "ServerInfoRequestMessage{}";
            }
        }

        /**
         * Response payload for a {@link ServerInfoRequestMessage}.
         */
        public static final class ServerInfoResponseMessage implements Message {
            private static final long serialVersionUID = 1L;

            private final String serverName;
            private final int currentPlayers;
            private final int requiredPlayers;
            private final boolean gameInProgress;
            private final List<PlayerInfo> players;

            public ServerInfoResponseMessage(String serverName, int currentPlayers, int requiredPlayers,
                                             boolean gameInProgress, List<PlayerInfo> players) {
                this.serverName = serverName;
                this.currentPlayers = currentPlayers;
                this.requiredPlayers = requiredPlayers;
                this.gameInProgress = gameInProgress;
                this.players = List.copyOf(players);
            }

            public String getServerName() {
                return serverName;
            }

            public int getCurrentPlayers() {
                return currentPlayers;
            }

            public int getRequiredPlayers() {
                return requiredPlayers;
            }

            public boolean isGameInProgress() {
                return gameInProgress;
            }

            public List<PlayerInfo> getPlayers() {
                return players;
            }
        }
    }

    // ------------------------------------------------------------------
    // Lobby lifecycle
    // ------------------------------------------------------------------

    final class Lobby {
        private Lobby() {
        }

        /**
         * Sent by a client immediately after connecting to announce itself.
         */
        public static final class JoinRequestMessage implements Message {
            private static final long serialVersionUID = 1L;

            private final String playerName;

            public JoinRequestMessage(String playerName) {
                this.playerName = playerName;
            }

            public String getPlayerName() {
                return playerName;
            }
        }

        /**
         * Sent by the server to confirm a player has joined and provide lobby context.
         */
        public static final class JoinAcceptedMessage implements Message {
            private static final long serialVersionUID = 1L;

            private final int playerId;
            private final Lane assignedLane;
            private final List<PlayerInfo> lobbyPlayers;
            private final int requiredPlayers;

            public JoinAcceptedMessage(int playerId, Lane assignedLane, List<PlayerInfo> lobbyPlayers,
                                       int requiredPlayers) {
                this.playerId = playerId;
                this.assignedLane = assignedLane;
                this.lobbyPlayers = List.copyOf(lobbyPlayers);
                this.requiredPlayers = requiredPlayers;
            }

            public int getPlayerId() {
                return playerId;
            }

            public Lane getAssignedLane() {
                return assignedLane;
            }

            public List<PlayerInfo> getLobbyPlayers() {
                return lobbyPlayers;
            }

            public int getRequiredPlayers() {
                return requiredPlayers;
            }
        }

        /**
         * Broadcast by the server whenever the lobby composition changes.
         */
        public static final class LobbyUpdateMessage implements Message {
            private static final long serialVersionUID = 1L;

            private final List<PlayerInfo> players;
            private final int requiredPlayers;

            public LobbyUpdateMessage(List<PlayerInfo> players, int requiredPlayers) {
                this.players = List.copyOf(players);
                this.requiredPlayers = requiredPlayers;
            }

            public List<PlayerInfo> getPlayers() {
                return players;
            }

            public int getRequiredPlayers() {
                return requiredPlayers;
            }
        }

        /**
         * Broadcast when a player disconnects before or during the game.
         */
        public static final class PlayerLeftMessage implements Message {
            private static final long serialVersionUID = 1L;

            private final int playerId;

            public PlayerLeftMessage(int playerId) {
                this.playerId = playerId;
            }

            public int getPlayerId() {
                return playerId;
            }
        }
    }

    // ------------------------------------------------------------------
    // Gameplay flow
    // ------------------------------------------------------------------

    final class Gameplay {
        private Gameplay() {
        }

        /**
         * Informs clients that the game is starting imminently.
         */
        public static final class StartGameMessage implements Message {
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
                this.chart = List.copyOf(chart);
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

        /**
         * Sent by clients whenever the assigned lane key is pressed.
         */
        public static final class PlayerInputMessage implements Message {
            private static final long serialVersionUID = 2L;

            private final int playerId;
            private final int noteId;
            private final long pressTimeMillis;
            private final long clientDeltaMillis;

            public PlayerInputMessage(int playerId, int noteId, long pressTimeMillis, long clientDeltaMillis) {
                this.playerId = playerId;
                this.noteId = noteId;
                this.pressTimeMillis = pressTimeMillis;
                this.clientDeltaMillis = clientDeltaMillis;
            }

            public int getPlayerId() {
                return playerId;
            }

            public int getNoteId() {
                return noteId;
            }

            public long getPressTimeMillis() {
                return pressTimeMillis;
            }

            public long getClientDeltaMillis() {
                return clientDeltaMillis;
            }
        }

        /**
         * Broadcast whenever a note is resolved for a player.
         */
        public static final class HitResultMessage implements Message {
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

        /**
         * Periodically broadcast scoreboard information from the server.
         */
        public static final class GameStateUpdateMessage implements Message {
            private static final long serialVersionUID = 1L;

            private final List<PlayerScore> scores;

            public GameStateUpdateMessage(List<PlayerScore> scores) {
                this.scores = List.copyOf(scores);
            }

            public List<PlayerScore> getScores() {
                return scores;
            }
        }

        /**
         * Indicates the match has finished.
         */
        public static final class GameOverMessage implements Message {
            private static final long serialVersionUID = 1L;

            private final List<PlayerScore> finalScores;

            public GameOverMessage(List<PlayerScore> finalScores) {
                this.finalScores = List.copyOf(finalScores);
            }

            public List<PlayerScore> getFinalScores() {
                return finalScores;
            }
        }
    }

    // ------------------------------------------------------------------
    // Post-game
    // ------------------------------------------------------------------

    final class PostGame {
        private PostGame() {
        }

        /**
         * Sent by a client once they press the play again button on the result screen.
         */
        public static final class PlayAgainRequestMessage implements Message {
            private static final long serialVersionUID = 1L;

            @Override
            public String toString() {
                return "PlayAgainRequestMessage{}";
            }
        }

        /**
         * Broadcast by the server while waiting for every connected player to ready up for the next
         * round.
         */
        public static final class ReplayStatusMessage implements Message {
            private static final long serialVersionUID = 1L;

            private final int readyPlayers;
            private final int totalPlayers;

            public ReplayStatusMessage(int readyPlayers, int totalPlayers) {
                this.readyPlayers = readyPlayers;
                this.totalPlayers = totalPlayers;
            }

            public int getReadyPlayers() {
                return readyPlayers;
            }

            public int getTotalPlayers() {
                return totalPlayers;
            }
        }
    }
}
