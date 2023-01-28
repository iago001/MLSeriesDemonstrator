package com.example.mlseriesdemonstrator.helpers.vision.drowsiness;

import com.google.mlkit.vision.face.Face;

import java.util.ArrayDeque;

public class FaceDrowsiness {
    private static final float DROWSINESS_THRESHOLD = 0.5f;
    private static final int MAX_HISTORY = 10;

    public long lastCheckedAt;
    private final ArrayDeque<Boolean> history = new ArrayDeque<>();

    public boolean isDrowsy(Face face) {
        boolean isDrowsy = true;
        lastCheckedAt = System.currentTimeMillis();
        if (face.getLeftEyeOpenProbability() == null
            || face.getRightEyeOpenProbability() == null) {
            return false;
        }
        if (face.getLeftEyeOpenProbability() < DROWSINESS_THRESHOLD
            && face.getRightEyeOpenProbability() < DROWSINESS_THRESHOLD) {
            history.addLast(true);
        } else {
            history.addLast(false);
        }
        if (history.size() > MAX_HISTORY) {
            history.removeFirst();
        }
        if (history.size() == MAX_HISTORY) {
            for (boolean instance : history) {
                isDrowsy &= instance;
            }
        } else {
            return false;
        }
        return isDrowsy;
    }
}
