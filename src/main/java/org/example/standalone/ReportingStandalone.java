package org.example.standalone;

import org.example.error.StationException;
import org.example.infra.Database;
import org.example.reporting.DefectClassifier;
import org.example.reporting.ReportDesk;
import org.example.reporting.ReportRepository;

public class ReportingStandalone {
    public static void main(String[] args) {
        Database db = new Database("reporting-standalone.db");
        ReportDesk desk = new ReportDesk(new ReportRepository(db), new DefectClassifier());

        System.out.println("Filed:   " + desk.fileReport("MODULE-4", 1));
        System.out.println("Filed:   " + desk.fileReport("MODULE-3", 0));

        desk.setSystemOnline(false);
        try {
            desk.fileReport("MODULE-5", 2);
        } catch (StationException e) {
            System.out.println("Refused: " + e.getMessage());
        }
        desk.setSystemOnline(true);

        System.out.println("On disk:");
        desk.all().forEach(r -> System.out.println("  " + r));
    }
}