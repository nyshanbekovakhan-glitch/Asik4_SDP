package org.example.airlock;

import org.example.error.UnsafeRadiationException;

public class RadiationAssessor {
    private final double maxSafeLevel;

    public RadiationAssessor(double maxSafeLevel) {
        if (maxSafeLevel <= 0) {
            throw new IllegalArgumentException("Safe limit must be positive");
        }
        this.maxSafeLevel = maxSafeLevel;
    }

    public boolean isSafe(double level) {
        return level <= maxSafeLevel;
    }

    public void requireSafe(double level) {
        if (!isSafe(level)) {
            throw new UnsafeRadiationException(level, maxSafeLevel);
        }
    }
}
