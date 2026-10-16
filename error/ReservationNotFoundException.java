package org.example.error;

public class ReservationNotFoundException extends StationException {
    public ReservationNotFoundException(String id) {
        super("Airlock reservation not found: " + id);
    }
}