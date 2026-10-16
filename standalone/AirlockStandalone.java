package org.example.standalone;

import org.example.airlock.AirlockReservation;
import org.example.airlock.AirlockReservationRepository;
import org.example.airlock.AirlockScheduler;
import org.example.airlock.RadiationAssessor;
import org.example.error.StationException;
import org.example.infra.Database;

public class AirlockStandalone {
    public static void main(String[] args) {
        Database db = new Database("airlock-standalone.db");
        AirlockScheduler airlock =
                new AirlockScheduler(new AirlockReservationRepository(db), new RadiationAssessor(1.0), 2);

        AirlockReservation first = airlock.reserve("MODULE-3");
        System.out.println("Reserved:  " + first);

        AirlockReservation second = airlock.reserve("MODULE-4");
        System.out.println("Reserved:  " + second);

        System.out.println("Confirmed: " + airlock.confirm(first.getId()));
        System.out.println("Released:  " + airlock.release(second.getId()));

        airlock.setRadiationLevel(5.0);
        try {
            airlock.reserve("MODULE-5");
        } catch (StationException e) {
            System.out.println("Refused:   " + e.getMessage());
        }
        airlock.setRadiationLevel(0.2);

        System.out.println("On disk:");
        airlock.all().forEach(r -> System.out.println("  " + r));
    }
}