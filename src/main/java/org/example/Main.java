package org.example;

import org.example.app.DemoCrashSimulator;
import org.example.app.StationContext;
import org.example.chain.ChainRecord;
import org.example.error.StationException;
import org.example.facade.InspectionResult;
import org.example.facade.StationAdminFacade;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final String DEFAULT_DB_FILE = "station.db";

    public static void main(String[] args) throws IOException {
        String dbFile = args.length > 0 ? args[0] : DEFAULT_DB_FILE;
        Scanner in = new Scanner(System.in);
        StationContext station = StationContext.open(dbFile);

        System.out.println("Space Station Inspection Robot");
        System.out.println("Database file: " + dbFile);

        while (true) {
            printMenu();
            String choice = ask(in, "Choose an option");
            if (choice.equals("0")) {
                break;
            }
            try {
                switch (choice) {
                    case "1" -> runInspection(station, in);
                    case "2" -> showState(station.admin());
                    case "3" -> setRadiation(station.admin(), in);
                    case "4" -> setRobotSignal(station.admin(), in);
                    case "5" -> setRobotBattery(station.admin(), in);
                    case "6" -> setReportSystem(station.admin(), in);
                    case "7" -> recover(station.admin());
                    case "8" -> compensateByHand(station.admin(), in);
                    case "9" -> simulateCrash(station.crashSimulator(), in);
                    case "10" -> {
                        if (ask(in, "Delete all saved data? Type yes").equalsIgnoreCase("yes")) {
                            Files.deleteIfExists(Path.of(dbFile));
                            station = StationContext.open(dbFile);
                            System.out.println("Database deleted. A clean one was created.");
                        }
                    }
                    default -> System.out.println("Unknown option: " + choice);
                }
            } catch (StationException e) {
                System.out.println("FAILED: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid input: " + e.getMessage());
            }
        }
        System.out.println("Bye. Everything is saved in " + dbFile);
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("1  Run an inspection (A -> B -> C)");
        System.out.println("2  Show what is saved (chains and all three subsystems)");
        System.out.println("3  Change radiation level");
        System.out.println("4  Switch robot signal on/off");
        System.out.println("5  Change robot battery");
        System.out.println("6  Switch report system on/off");
        System.out.println("7  Recover unfinished chains (after a restart)");
        System.out.println("8  Compensate one chain by hand");
        System.out.println("9  Simulate a crash in the middle of a chain");
        System.out.println("10 Reset the database");
        System.out.println("0  Exit");
    }

    private static String ask(Scanner in, String prompt) {
        System.out.print(prompt + ": ");
        return in.hasNextLine() ? in.nextLine().trim() : "0";
    }

    private static void runInspection(StationContext station, Scanner in) {
        String moduleId = ask(in, "Module id (for example MODULE-4)");
        try {
            InspectionResult result = station.inspection().runInspection(moduleId);
            System.out.println("DONE: " + result);
        } catch (StationException e) {
            System.out.println("FAILED: " + e.getMessage());
            System.out.println("Choose option 2 to see what was undone.");
        }
    }

    private static void showState(StationAdminFacade admin) {
        System.out.println();
        System.out.println("Environment: " + admin.environmentSummary());
        System.out.println("Chains (facade journal):");
        printAll(admin.chains());
        System.out.println("A: airlock reservations:");
        printAll(admin.reservations());
        System.out.println("B: inspection runs:");
        printAll(admin.runs());
        System.out.println("C: reports:");
        printAll(admin.reports());
    }

    private static void printAll(List<?> items) {
        if (items.isEmpty()) {
            System.out.println("   (none)");
        }
        items.forEach(item -> System.out.println("   " + item));
    }

    private static void setRadiation(StationAdminFacade admin, Scanner in) {
        double level = Double.parseDouble(ask(in, "New radiation level in mSv/h (safe limit is 1.0)"));
        admin.setRadiationLevel(level);
        System.out.println("Radiation is now " + level + " mSv/h");
    }

    private static void setRobotSignal(StationAdminFacade admin, Scanner in) {
        boolean online = parseOnOff(ask(in, "Robot signal (on/off)"));
        admin.setRobotSignal(online);
        System.out.println("Robot signal is now " + (online ? "ON" : "OFF"));
    }

    private static void setRobotBattery(StationAdminFacade admin, Scanner in) {
        int level = Integer.parseInt(ask(in, "Robot battery in percent (at least 20 is needed)"));
        if (level < 0 || level > 100) {
            throw new IllegalArgumentException("battery must be between 0 and 100");
        }
        admin.setRobotBattery(level);
        System.out.println("Robot battery is now " + level + "%");
    }

    private static void setReportSystem(StationAdminFacade admin, Scanner in) {
        boolean online = parseOnOff(ask(in, "Report system (on/off)"));
        admin.setReportSystemOnline(online);
        System.out.println("Report system is now " + (online ? "ON" : "OFF"));
    }

    private static boolean parseOnOff(String text) {
        if (text.equalsIgnoreCase("on")) {
            return true;
        }
        if (text.equalsIgnoreCase("off")) {
            return false;
        }
        throw new IllegalArgumentException("please type on or off");
    }

    private static void recover(StationAdminFacade admin) {
        System.out.println("Unfinished before recovery:");
        printAll(admin.unfinishedChains());
        int count = admin.recoverAfterRestart();
        System.out.println("Recovered " + count + " unfinished chain(s). Choose option 2 to see the result.");
    }

    private static void compensateByHand(StationAdminFacade admin, Scanner in) {
        System.out.println("Unfinished chains:");
        printAll(admin.unfinishedChains());
        String chainId = ask(in, "Chain id (for example CH-1a2b3c4d)");
        ChainRecord chain = admin.compensateManually(chainId);
        System.out.println("DONE: " + chain);
    }

    private static void simulateCrash(DemoCrashSimulator simulator, Scanner in) {
        System.out.println("1  crash after the airlock was reserved (robot not started)");
        System.out.println("2  crash after the robot finished (report not filed)");
        String kind = ask(in, "Choose");
        if (!kind.equals("1") && !kind.equals("2")) {
            System.out.println("Unknown option: " + kind);
            return;
        }
        String moduleId = ask(in, "Module id (for example MODULE-4)");
        ChainRecord chain = kind.equals("1")
                ? simulator.leaveChainWaitingForRobot(moduleId)
                : simulator.leaveChainWaitingForReport(moduleId);
        System.out.println("Chain left unfinished: " + chain);
        System.out.println("Nothing was undone. Exit (0) and start again, or choose option 7.");
    }
}