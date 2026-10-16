package org.example.app;

import org.example.error.StationException;
import org.example.facade.InspectionFacade;
import org.example.facade.StationAdminFacade;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FacadeDemo {
    private static final String DB_FILE = "facade-demo.db";

    public static void main(String[] args) throws IOException {
        Files.deleteIfExists(Path.of(DB_FILE));
        StationContext station = StationContext.open(DB_FILE);
        InspectionFacade facade = station.inspection();
        StationAdminFacade admin = station.admin();

        System.out.println("1. Normal inspection");
        attempt(facade, "MODULE-4");

        System.out.println("2. B fails: robot loses signal");
        admin.setRobotSignal(false);
        attempt(facade, "MODULE-5");
        admin.setRobotSignal(true);

        System.out.println("3. C fails: report system offline");
        admin.setReportSystemOnline(false);
        attempt(facade, "MODULE-7");
        admin.setReportSystemOnline(true);

        System.out.println("4. Nothing starts: radiation too high");
        admin.setRadiationLevel(5.0);
        attempt(facade, "MODULE-8");
        admin.setRadiationLevel(0.2);

        System.out.println("5. Nothing starts: invalid module id");
        attempt(facade, "abc");

        printState(admin);
    }

    private static void attempt(InspectionFacade facade, String moduleId) {
        try {
            System.out.println("   OK:     " + facade.runInspection(moduleId));
        } catch (StationException e) {
            System.out.println("   FAILED: " + e.getMessage());
        }
    }

    private static void printState(StationAdminFacade admin) {
        System.out.println();
        System.out.println("Chains (facade log):");
        admin.chains().forEach(c -> System.out.println("   " + c));
        System.out.println("A: airlock reservations:");
        admin.reservations().forEach(r -> System.out.println("   " + r));
        System.out.println("B: inspection runs:");
        admin.runs().forEach(r -> System.out.println("   " + r));
        System.out.println("C: reports:");
        admin.reports().forEach(r -> System.out.println("   " + r));
    }
}