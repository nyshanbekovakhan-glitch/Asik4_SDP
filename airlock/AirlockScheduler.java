package org.example.airlock;

import org.example.error.InvalidModuleException;
import org.example.error.NoAirlockAvailableException;
import org.example.error.ReservationNotFoundException;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class AirlockScheduler {
    private static final Pattern MODULE_ID = Pattern.compile("[A-Z]+-\\d+");

    private final AirlockReservationRepository repository;
    private final RadiationAssessor assessor;
    private final int capacity;

    public AirlockScheduler(AirlockReservationRepository repository,
                            RadiationAssessor assessor,
                            int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        this.repository = repository;
        this.assessor = assessor;
        this.capacity = capacity;
    }

    public AirlockReservation reserve(String moduleId) {
        if (moduleId == null || !MODULE_ID.matcher(moduleId).matches()) {
            throw new InvalidModuleException(moduleId);
        }
        assessor.requireSafe(repository.loadRadiationLevel());
        if (repository.countByStatus(AirlockReservation.Status.RESERVED) >= capacity) {
            throw new NoAirlockAvailableException(capacity);
        }
        AirlockReservation reservation =
                new AirlockReservation("RES-" + UUID.randomUUID().toString().substring(0, 8), moduleId);
        repository.save(reservation);
        return reservation;
    }

    public AirlockReservation confirm(String reservationId) {
        AirlockReservation reservation = get(reservationId);
        reservation.confirm();
        repository.save(reservation);
        return reservation;
    }

    public AirlockReservation release(String reservationId) {
        AirlockReservation reservation = get(reservationId);
        if (reservation.getStatus() == AirlockReservation.Status.RELEASED) {
            return reservation;
        }
        reservation.release();
        repository.save(reservation);
        return reservation;
    }

    public AirlockReservation get(String reservationId) {
        return repository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));
    }

    public List<AirlockReservation> all() {
        return repository.findAll();
    }

    public double radiationLevel() {
        return repository.loadRadiationLevel();
    }

    public void setRadiationLevel(double level) {
        repository.saveRadiationLevel(level);
    }
}
