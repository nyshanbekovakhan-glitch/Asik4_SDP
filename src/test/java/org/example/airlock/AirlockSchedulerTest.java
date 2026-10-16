package org.example.airlock;

import org.example.airlock.AirlockReservation.Status;
import org.example.error.IllegalStatusTransitionException;
import org.example.error.InvalidModuleException;
import org.example.error.NoAirlockAvailableException;
import org.example.error.ReservationNotFoundException;
import org.example.error.UnsafeRadiationException;
import org.example.infra.Database;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AirlockSchedulerTest {

    @TempDir
    Path tempDir;

    private AirlockScheduler airlockWithCapacity(int capacity) {
        Database db = new Database(tempDir.resolve("station.db").toString());
        return new AirlockScheduler(new AirlockReservationRepository(db), new RadiationAssessor(1.0), capacity);
    }

    private AirlockScheduler airlockAfterRestart(int capacity) {
        return airlockWithCapacity(capacity);
    }

    private void assertStatusOnDisk(AirlockScheduler airlock, AirlockReservation reservation, Status expected) {
        assertEquals(expected, airlock.get(reservation.getId()).getStatus());
    }

    @Test
    void reservingStoresAReservedReservationOnDisk() {
        AirlockScheduler airlock = airlockWithCapacity(2);

        AirlockReservation reservation = airlock.reserve("MODULE-3");

        assertStatusOnDisk(airlockAfterRestart(2), reservation, Status.RESERVED);
        assertEquals("MODULE-3", airlockAfterRestart(2).get(reservation.getId()).getModuleId());
    }

    @Test
    void unsafeRadiationRefusesReservationAndSavesNothing() {
        AirlockScheduler airlock = airlockWithCapacity(2);
        airlock.setRadiationLevel(5.0);

        assertThrows(UnsafeRadiationException.class, () -> airlock.reserve("MODULE-3"));

        assertTrue(airlock.all().isEmpty());
    }

    @Test
    void radiationLevelStartsLowAndSurvivesRestart() {
        assertEquals(0.2, airlockWithCapacity(2).radiationLevel());

        airlockWithCapacity(2).setRadiationLevel(4.2);

        assertEquals(4.2, airlockAfterRestart(2).radiationLevel());
    }

    @Test
    void badModuleIdsAreRefusedAndNothingIsSaved() {
        AirlockScheduler airlock = airlockWithCapacity(2);

        assertThrows(InvalidModuleException.class, () -> airlock.reserve(null));
        assertThrows(InvalidModuleException.class, () -> airlock.reserve(""));
        assertThrows(InvalidModuleException.class, () -> airlock.reserve("module-3"));
        assertThrows(InvalidModuleException.class, () -> airlock.reserve("MODULE3"));

        assertTrue(airlock.all().isEmpty());
    }

    @Test
    void whenAllSlotsAreHeldTheNextReservationIsRefused() {
        AirlockScheduler airlock = airlockWithCapacity(2);
        airlock.reserve("MODULE-1");
        airlock.reserve("MODULE-2");

        assertThrows(NoAirlockAvailableException.class, () -> airlock.reserve("MODULE-3"));

        assertEquals(2, airlock.all().size());
    }

    @Test
    void releasingFreesTheSlotForSomeoneElse() {
        AirlockScheduler airlock = airlockWithCapacity(1);
        AirlockReservation first = airlock.reserve("MODULE-1");

        airlock.release(first.getId());
        AirlockReservation second = airlock.reserve("MODULE-2");

        assertStatusOnDisk(airlock, first, Status.RELEASED);
        assertStatusOnDisk(airlock, second, Status.RESERVED);
    }

    @Test
    void confirmingAlsoFreesTheSlot() {
        AirlockScheduler airlock = airlockWithCapacity(1);
        AirlockReservation first = airlock.reserve("MODULE-1");

        airlock.confirm(first.getId());
        airlock.reserve("MODULE-2");

        assertStatusOnDisk(airlock, first, Status.CONFIRMED);
    }

    @Test
    void releasingTwiceIsHarmless() {
        AirlockScheduler airlock = airlockWithCapacity(2);
        AirlockReservation reservation = airlock.reserve("MODULE-1");

        airlock.release(reservation.getId());
        airlock.release(reservation.getId());

        assertStatusOnDisk(airlock, reservation, Status.RELEASED);
    }

    @Test
    void aConfirmedReservationCannotBeReleasedAnymore() {
        AirlockScheduler airlock = airlockWithCapacity(2);
        AirlockReservation reservation = airlock.reserve("MODULE-1");
        airlock.confirm(reservation.getId());

        assertThrows(IllegalStatusTransitionException.class, () -> airlock.release(reservation.getId()));

        assertStatusOnDisk(airlock, reservation, Status.CONFIRMED);
    }

    @Test
    void unknownReservationIdIsReported() {
        AirlockScheduler airlock = airlockWithCapacity(2);

        assertThrows(ReservationNotFoundException.class, () -> airlock.get("RES-nope"));
    }

    @Test
    void capacityMustBePositive() {
        Database db = new Database(tempDir.resolve("x.db").toString());
        assertThrows(IllegalArgumentException.class,
                () -> new AirlockScheduler(new AirlockReservationRepository(db), new RadiationAssessor(1.0), 0));
    }
}