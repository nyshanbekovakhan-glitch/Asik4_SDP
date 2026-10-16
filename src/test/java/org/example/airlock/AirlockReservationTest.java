package org.example.airlock;

import org.example.airlock.AirlockReservation.Status;
import org.example.error.IllegalStatusTransitionException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AirlockReservationTest {

    private AirlockReservation aFreshReservation() {
        return new AirlockReservation("RES-1", "MODULE-1");
    }

    @Test
    void newReservationStartsAsReserved() {
        assertEquals(Status.RESERVED, aFreshReservation().getStatus());
    }

    @Test
    void reservedCanBeConfirmed() {
        AirlockReservation reservation = aFreshReservation();
        reservation.confirm();
        assertEquals(Status.CONFIRMED, reservation.getStatus());
    }

    @Test
    void reservedCanBeReleased() {
        AirlockReservation reservation = aFreshReservation();
        reservation.release();
        assertEquals(Status.RELEASED, reservation.getStatus());
    }

    @Test
    void confirmedReservationCannotBeReleased() {
        AirlockReservation reservation = aFreshReservation();
        reservation.confirm();
        assertThrows(IllegalStatusTransitionException.class, reservation::release);
    }

    @Test
    void releasedReservationCannotBeConfirmed() {
        AirlockReservation reservation = aFreshReservation();
        reservation.release();
        assertThrows(IllegalStatusTransitionException.class, reservation::confirm);
    }

    @Test
    void toStringShowsIdModuleAndStatus() {
        assertEquals("RES-1 [MODULE-1, RESERVED]", aFreshReservation().toString());
    }
}