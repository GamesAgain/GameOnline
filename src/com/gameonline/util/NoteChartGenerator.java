package com.gameonline.util;

import com.gameonline.model.Lane;
import com.gameonline.model.NoteData;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Utility that creates a pseudo-random chart so the prototype can be played without
 * external song files.
 */
public final class NoteChartGenerator {
    private NoteChartGenerator() {
    }

    public static List<NoteData> generate(long durationMillis) {
        List<NoteData> notes = new ArrayList<>();
        Random random = new Random();
        int id = 1;
        long time = 1000L; // start after 1 second
        while (time < durationMillis - 1000L) {
            Lane lane = Lane.fromIndex(random.nextInt(3));
            notes.add(new NoteData(id++, lane, time));
            long spacing = 300L + random.nextInt(300);
            time += spacing;
        }
        return notes;
    }
}
