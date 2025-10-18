package com.gameonline.engine;

import com.gameonline.model.Judgement;
import com.gameonline.model.Lane;
import com.gameonline.model.NoteData;
import com.gameonline.model.PlayerScore;
import com.gameonline.network.messages.GameStateUpdateMessage;
import com.gameonline.network.messages.HitResultMessage;
import com.gameonline.util.GameRules;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

/**
 * Core gameplay loop running on the server. Processes player inputs, resolves notes and
 * communicates score updates back to the networking layer.
 */
public final class GameEngine implements Runnable {
    private final Map<Integer, PlayerState> playersById;
    private final Map<Lane, PlayerState> playersByLane;
    private final List<ServerNote> notes;
    private final Queue<PlayerInput> inputQueue = new ConcurrentLinkedQueue<>();
    private final Consumer<HitResultMessage> hitCallback;
    private final Consumer<GameStateUpdateMessage> stateCallback;
    private final Consumer<List<PlayerScore>> finishCallback;
    private final long songDurationMillis;

    private volatile boolean running;
    private long startTimeMillis;
    private long lastStateBroadcast;

    public GameEngine(Map<Integer, PlayerState> players,
                      List<NoteData> chart,
                      Consumer<HitResultMessage> hitCallback,
                      Consumer<GameStateUpdateMessage> stateCallback,
                      Consumer<List<PlayerScore>> finishCallback,
                      long songDurationMillis) {
        this.playersById = new ConcurrentHashMap<>(players);
        this.playersByLane = new ConcurrentHashMap<>();
        for (PlayerState state : players.values()) {
            playersByLane.put(state.getLane(), state);
        }
        this.notes = new ArrayList<>();
        for (NoteData data : chart) {
            notes.add(new ServerNote(data));
        }
        notes.sort(Comparator.comparingLong(ServerNote::getHitTimeMillis));
        this.hitCallback = hitCallback;
        this.stateCallback = stateCallback;
        this.finishCallback = finishCallback;
        this.songDurationMillis = songDurationMillis;
    }

    public void start(long startTimeMillis) {
        this.startTimeMillis = startTimeMillis;
        this.running = true;
        Thread thread = new Thread(this, "GameEngineThread");
        thread.start();
    }

    public void enqueueInput(PlayerInput input) {
        inputQueue.add(input);
    }

    @Override
    public void run() {
        try {
            while (running) {
                long now = System.currentTimeMillis();
                if (now < startTimeMillis) {
                    Thread.sleep(5L);
                    continue;
                }

                processInputs(now);
                processMisses(now);
                maybeBroadcastState(now);

                if (now - startTimeMillis >= songDurationMillis + GameRules.MISS_WINDOW_MS) {
                    running = false;
                }

                Thread.sleep(5L);
            }
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        } finally {
            finishCallback.accept(collectScores());
        }
    }

    private void processInputs(long now) {
        PlayerInput input;
        while ((input = inputQueue.poll()) != null) {
            PlayerState player = playersById.get(input.getPlayerId());
            if (player == null) {
                continue;
            }
            long pressTime = input.getPressTimeMillis();
            long deltaFromStart = pressTime - startTimeMillis;
            if (deltaFromStart < -GameRules.MISS_WINDOW_MS) {
                // Ignore very early presses.
                continue;
            }

            ServerNote candidate = findCandidateNote(player.getLane(), pressTime);
            if (candidate == null) {
                registerMiss(player, -1);
                continue;
            }

            long scheduledTime = startTimeMillis + candidate.getHitTimeMillis();
            long delta = pressTime - scheduledTime;
            if (Math.abs(delta) > GameRules.MISS_WINDOW_MS) {
                registerMiss(player, candidate.getId());
                continue;
            }

            Judgement judgement = GameRules.judgementForDelta(delta);
            if (judgement == Judgement.MISS) {
                registerMiss(player, candidate.getId());
                continue;
            }

            candidate.markHit();
            player.registerJudgement(judgement.name(), true);
            int deltaScore = GameRules.calculateScore(judgement, player.getCombo());
            player.applyScore(deltaScore, false);
            hitCallback.accept(new HitResultMessage(player.getId(), candidate.getId(), judgement.name(),
                    player.getScore(), player.getCombo()));
        }
    }

    private void processMisses(long now) {
        for (ServerNote note : notes) {
            if (note.isHit() || note.isMissed()) {
                continue;
            }
            long scheduled = startTimeMillis + note.getHitTimeMillis();
            if (now > scheduled + GameRules.MISS_WINDOW_MS) {
                note.markMissed();
                PlayerState player = playersByLane.get(note.getLane());
                if (player != null) {
                    registerMiss(player, note.getId());
                }
            }
        }
    }

    private void maybeBroadcastState(long now) {
        if (now - lastStateBroadcast >= 200L) {
            lastStateBroadcast = now;
            stateCallback.accept(new GameStateUpdateMessage(collectScores()));
        }
    }

    private List<PlayerScore> collectScores() {
        List<PlayerScore> snapshots = new ArrayList<>();
        for (PlayerState state : playersById.values()) {
            snapshots.add(state.toSnapshot());
        }
        return new ArrayList<>(snapshots);
    }

    private ServerNote findCandidateNote(Lane lane, long pressTime) {
        ServerNote best = null;
        long bestDelta = Long.MAX_VALUE;
        for (ServerNote note : notes) {
            if (note.isHit() || note.isMissed() || note.getLane() != lane) {
                continue;
            }
            long scheduledTime = startTimeMillis + note.getHitTimeMillis();
            long delta = Math.abs(pressTime - scheduledTime);
            if (delta < bestDelta) {
                best = note;
                bestDelta = delta;
            }
        }
        if (best != null && bestDelta <= GameRules.MISS_WINDOW_MS) {
            return best;
        }
        return null;
    }

    private void registerMiss(PlayerState player, int noteId) {
        player.registerJudgement(Judgement.MISS.name(), false);
        player.applyScore(0, true);
        hitCallback.accept(new HitResultMessage(player.getId(), noteId, Judgement.MISS.name(),
                player.getScore(), player.getCombo()));
    }
}
