package org.example.standalone;

import org.example.error.StationException;
import org.example.infra.Database;
import org.example.robot.InspectionRobotController;
import org.example.robot.InspectionRunRepository;
import org.example.robot.LeakSensor;

public class RobotStandalone {
    public static void main(String[] args) {
        Database db = new Database("robot-standalone.db");
        InspectionRobotController robot = new InspectionRobotController(
                new InspectionRunRepository(db), new LeakSensor(), 20);

        System.out.println("Done:    " + robot.inspect("MODULE-4"));

        robot.setSignalOnline(false);
        try {
            robot.inspect("MODULE-5");
        } catch (StationException e) {
            System.out.println("Refused: " + e.getMessage());
        }
        robot.setSignalOnline(true);

        robot.setBattery(10);
        try {
            robot.inspect("MODULE-6");
        } catch (StationException e) {
            System.out.println("Refused: " + e.getMessage());
        }
        robot.setBattery(100);

        System.out.println("On disk:");
        robot.all().forEach(r -> System.out.println("  " + r));
    }
}