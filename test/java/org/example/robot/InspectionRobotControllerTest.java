package org.example.robot;

import org.example.error.InspectionNotFoundException;
import org.example.error.RobotBatteryLowException;
import org.example.error.RobotLinkLostException;
import org.example.infra.Database;
import org.example.robot.InspectionRun.Status;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InspectionRobotControllerTest {

    @TempDir
    Path tempDir;

    private InspectionRobotController aRobot() {
        Database db = new Database(tempDir.resolve("station.db").toString());
        return new InspectionRobotController(new InspectionRunRepository(db), new LeakSensor(), 20);
    }

    private InspectionRobotController theSameRobotAfterRestart() {
        return aRobot();
    }

    @Test
    void inspectingAGoodModuleCompletesTheRunAndStoresIt() {
        InspectionRun run = aRobot().inspect("MODULE-4");

        InspectionRun onDisk = theSameRobotAfterRestart().get(run.getId());
        assertEquals(Status.COMPLETED, onDisk.getStatus());
        assertEquals(1, onDisk.getDefectCount());
    }

    @Test
    void losingTheSignalAbortsTheRunAndFails() {
        InspectionRobotController robot = aRobot();
        robot.setSignalOnline(false);

        assertThrows(RobotLinkLostException.class, () -> robot.inspect("MODULE-5"));

        assertEquals(1, robot.all().size());
        assertEquals(Status.ABORTED, robot.all().get(0).getStatus());
    }

    @Test
    void aLowBatteryRefusesTheInspectionAndSavesNothing() {
        InspectionRobotController robot = aRobot();
        robot.setBattery(10);

        assertThrows(RobotBatteryLowException.class, () -> robot.inspect("MODULE-6"));

        assertTrue(robot.all().isEmpty());
    }

    @Test
    void signalAndBatterySurviveARestart() {
        InspectionRobotController robot = aRobot();
        assertTrue(robot.isSignalOnline());
        assertEquals(100, robot.battery());

        robot.setSignalOnline(false);
        robot.setBattery(55);

        InspectionRobotController restarted = theSameRobotAfterRestart();
        assertFalse(restarted.isSignalOnline());
        assertEquals(55, restarted.battery());
    }

    @Test
    void abortingACompletedRunMakesItAbortedWithNoDefects() {
        InspectionRobotController robot = aRobot();
        InspectionRun run = robot.inspect("MODULE-4");

        robot.abort(run.getId());

        InspectionRun onDisk = robot.get(run.getId());
        assertEquals(Status.ABORTED, onDisk.getStatus());
        assertEquals(0, onDisk.getDefectCount());
    }

    @Test
    void abortingTwiceIsHarmless() {
        InspectionRobotController robot = aRobot();
        InspectionRun run = robot.inspect("MODULE-4");

        robot.abort(run.getId());
        robot.abort(run.getId());

        assertEquals(Status.ABORTED, robot.get(run.getId()).getStatus());
    }

    @Test
    void anUnknownRunIdIsReported() {
        assertThrows(InspectionNotFoundException.class, () -> aRobot().get("RUN-nope"));
    }
}