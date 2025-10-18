package com.gameonline.model;

import java.awt.Color;

/**
 * Represents one of the three playable lanes in the rhythm game.
 */
public enum Lane {
    BLUE(0, new Color(0x4A90E2)),
    YELLOW(1, new Color(0xF5A623)),
    RED(2, new Color(0xD0021B));

    private final int index;
    private final Color color;

    Lane(int index, Color color) {
        this.index = index;
        this.color = color;
    }

    public int getIndex() {
        return index;
    }

    public Color getColor() {
        return color;
    }

    public static Lane fromIndex(int idx) {
        for (Lane lane : values()) {
            if (lane.index == idx) {
                return lane;
            }
        }
        throw new IllegalArgumentException("Unknown lane index: " + idx);
    }
}
