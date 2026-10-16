package org.example.facade;

import org.example.airlock.AirlockReservation;
import org.example.airlock.AirlockReservationRepository;
import org.example.airlock.AirlockScheduler;
import org.example.airlock.RadiationAssessor;
import org.example.app.StationContext;
import org.example.chain.ChainLogRepository;
import org.example.chain.ChainRecord;
import org.example.chain.ChainStatus;
import org.example.error.IllegalStatusTransitionException;
import org.example.error.StationException;
import org.example.infra.Database;
import org.example.reporting.InspectionReport;
import org.example.robot.InspectionRobotController;
import org.example.robot.InspectionRun;
import org.example.robot.InspectionRunRepository;
import org.example.robot.LeakSensor;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StationScenario {
    private final String dbPath;
    private StationContext station;
    private InspectionResult result;
    private StationException failure;
    private int recovered;

    private StationScenario(String dbPath) {
        this.dbPath = dbPath;
        this.station = StationContext.open(dbPath);
    }

    static StationScenario aStation(Path folder) {
        return new StationScenario(folder.resolve("station.db").toString());
    }

    StationScenario robotLosesSignal() {
        station.admin().setRobotSignal(false);
        return this;
    }

    StationScenario reportSystemGoesOffline() {
        station.admin().setReportSystemOnline(false);
        return this;
    }

    StationScenario radiationRisesTo(double level) {
        station.admin().setRadiationLevel(level);
        return this;
    }

    StationScenario inspecting(String moduleId) {
        result = null;
        failure = null;
        try {
            result = station.inspection().runInspection(moduleId);
        } catch (StationException e) {
            failure = e;
        }
        return this;
    }

    StationScenario aCrashLeftTheChainWaitingForRobot(String moduleId) {
        Parts parts = new Parts(dbPath);
        AirlockReservation reservation = parts.airlock.reserve(moduleId);
        parts.chainLog.save(new ChainRecord("CH-crash-" + moduleId, moduleId, reservation.getId()));
        return this;
    }

    StationScenario aCrashLeftTheChainWaitingForReport(String moduleId) {
        Parts parts = new Parts(dbPath);
        AirlockReservation reservation = parts.airlock.reserve(moduleId);
        InspectionRun run = parts.robot.inspect(moduleId);
        ChainRecord chain = new ChainRecord("CH-crash-" + moduleId, moduleId, reservation.getId());
        chain.markBDone(run.getId());
        parts.chainLog.save(chain);
        return this;
    }

    StationScenario afterRestart() {
        station = StationContext.open(dbPath);
        return this;
    }

    StationScenario recoveringAfterRestart() {
        recovered = station.admin().recoverAfterRestart();
        return this;
    }

    StationScenario manuallyCompensatingTheOnlyChain() {
        station.admin().compensateManually(onlyChain().getId());
        return this;
    }

    StationScenario thenTheInspectionSucceeded() {
        assertNull(failure, "the inspection should have succeeded");
        assertNotNull(result, "the inspection should have returned a result");
        return this;
    }

    StationScenario thenTheInspectionFailedWith(Class<? extends StationException> type) {
        assertNotNull(failure, "the inspection should have failed");
        assertTrue(type.isInstance(failure),
                "expected " + type.getSimpleName() + " but got " + failure.getClass().getSimpleName());
        return this;
    }

    StationScenario thenTheOnlyChainIs(ChainStatus status) {
        assertEquals(status, onlyChain().getStatus());
        return this;
    }

    StationScenario thenTheChainHadNoRobotRunRecorded() {
        assertNull(onlyChain().getRunId(), "the chain should not know any robot run");
        return this;
    }

    StationScenario thenTheChainRememberedTheRobotRun() {
        assertNotNull(onlyChain().getRunId(), "the chain should remember the robot run");
        return this;
    }

    StationScenario thenTheAirlockReservationIs(AirlockReservation.Status status) {
        assertEquals(status, only(station.admin().reservations()).getStatus());
        return this;
    }

    StationScenario thenTheRobotRunIs(InspectionRun.Status status) {
        assertEquals(status, only(station.admin().runs()).getStatus());
        return this;
    }

    StationScenario thenTheReportIs(InspectionReport.Status status) {
        assertEquals(status, only(station.admin().reports()).getStatus());
        return this;
    }

    StationScenario thenNoRobotRunExists() {
        assertTrue(station.admin().runs().isEmpty(), "no robot run should exist");
        return this;
    }

    StationScenario thenNoReportExists() {
        assertTrue(station.admin().reports().isEmpty(), "no report should exist");
        return this;
    }

    StationScenario thenNothingWasSavedAnywhere() {
        assertTrue(station.admin().chains().isEmpty(), "chains should be empty");
        assertTrue(station.admin().reservations().isEmpty(), "reservations should be empty");
        assertTrue(station.admin().runs().isEmpty(), "robot runs should be empty");
        assertTrue(station.admin().reports().isEmpty(), "reports should be empty");
        return this;
    }

    StationScenario thenRecoveredChains(int count) {
        assertEquals(count, recovered);
        return this;
    }

    StationScenario thenManualCompensationIsRefused() {
        String chainId = onlyChain().getId();
        assertThrows(IllegalStatusTransitionException.class,
                () -> station.admin().compensateManually(chainId));
        return this;
    }

    private ChainRecord onlyChain() {
        return only(station.admin().chains());
    }

    private static <T> T only(List<T> items) {
        assertEquals(1, items.size(), "expected exactly one item but found " + items);
        return items.get(0);
    }

    private static final class Parts {
        final AirlockScheduler airlock;
        final InspectionRobotController robot;
        final ChainLogRepository chainLog;

        Parts(String dbPath) {
            Database db = new Database(dbPath);
            airlock = new AirlockScheduler(new AirlockReservationRepository(db), new RadiationAssessor(1.0), 2);
            robot = new InspectionRobotController(new InspectionRunRepository(db), new LeakSensor(), 20);
            chainLog = new ChainLogRepository(db);
        }
    }
}