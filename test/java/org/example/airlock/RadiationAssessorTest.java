package org.example.airlock;

import org.example.error.UnsafeRadiationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RadiationAssessorTest {
    private final RadiationAssessor assessor = new RadiationAssessor(1.0);

    @Test
    void levelBelowOrAtLimitIsSafe() {
        assertTrue(assessor.isSafe(0.5));
        assertTrue(assessor.isSafe(1.0));
    }

    @Test
    void levelAboveLimitIsNotSafe() {
        assertFalse(assessor.isSafe(1.1));
    }

    @Test
    void requireSafeThrowsOnlyWhenUnsafe() {
        assertDoesNotThrow(() -> assessor.requireSafe(0.2));
        assertThrows(UnsafeRadiationException.class, () -> assessor.requireSafe(3.0));
    }

    @Test
    void limitMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> new RadiationAssessor(0));
    }
}