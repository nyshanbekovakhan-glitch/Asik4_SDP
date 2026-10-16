package org.example.robot;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LeakSensorTest {
    private final LeakSensor sensor = new LeakSensor();

    @Test
    void theNumberOfLeaksDependsOnTheModuleNumber() {
        assertEquals(0, sensor.detect("MODULE-3"));
        assertEquals(1, sensor.detect("MODULE-4"));
        assertEquals(2, sensor.detect("MODULE-5"));
        assertEquals(0, sensor.detect("MODULE-6"));
    }

    @Test
    void aModuleIdWithoutANumberIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> sensor.detect("MODULE-x"));
    }
}