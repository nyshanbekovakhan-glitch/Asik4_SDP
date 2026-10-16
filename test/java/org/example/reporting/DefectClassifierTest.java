package org.example.reporting;

import org.example.reporting.DefectClassifier.Severity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefectClassifierTest {
    private final DefectClassifier classifier = new DefectClassifier();

    @Test
    void severityGrowsWithTheNumberOfDefects() {
        assertEquals(Severity.NONE, classifier.classify(0));
        assertEquals(Severity.MINOR, classifier.classify(1));
        assertEquals(Severity.MAJOR, classifier.classify(2));
        assertEquals(Severity.CRITICAL, classifier.classify(3));
        assertEquals(Severity.CRITICAL, classifier.classify(10));
    }

    @Test
    void aNegativeDefectCountIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> classifier.classify(-1));
    }

    @Test
    void onlyRealDefectsNeedARepairTicket() {
        assertFalse(classifier.needsRepairTicket(Severity.NONE));
        assertTrue(classifier.needsRepairTicket(Severity.MINOR));
        assertTrue(classifier.needsRepairTicket(Severity.MAJOR));
        assertTrue(classifier.needsRepairTicket(Severity.CRITICAL));
    }
}