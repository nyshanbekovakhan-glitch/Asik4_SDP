package org.example.app;

import org.example.airlock.AirlockReservationRepository;
import org.example.airlock.AirlockScheduler;
import org.example.airlock.RadiationAssessor;
import org.example.chain.ChainLogRepository;
import org.example.chain.ChainRecovery;
import org.example.facade.InspectionFacade;
import org.example.facade.StationAdminFacade;
import org.example.infra.Database;
import org.example.reporting.DefectClassifier;
import org.example.reporting.ReportDesk;
import org.example.reporting.ReportRepository;
import org.example.robot.InspectionRobotController;
import org.example.robot.InspectionRunRepository;
import org.example.robot.LeakSensor;

public final class StationContext {
    private final InspectionFacade inspection;
    private final StationAdminFacade admin;
    private final DemoCrashSimulator crashSimulator;

    private StationContext(InspectionFacade inspection,
                           StationAdminFacade admin,
                           DemoCrashSimulator crashSimulator) {
        this.inspection = inspection;
        this.admin = admin;
        this.crashSimulator = crashSimulator;
    }

    public static StationContext open(String dbPath) {
        Database db = new Database(dbPath);

        AirlockScheduler airlock = new AirlockScheduler(
                new AirlockReservationRepository(db), new RadiationAssessor(1.0), 2);
        InspectionRobotController robot = new InspectionRobotController(
                new InspectionRunRepository(db), new LeakSensor(), 20);
        ReportDesk reporting = new ReportDesk(new ReportRepository(db), new DefectClassifier());

        ChainLogRepository chainLog = new ChainLogRepository(db);
        ChainRecovery recovery = new ChainRecovery(airlock, robot, chainLog);

        return new StationContext(
                new InspectionFacade(airlock, robot, reporting, chainLog, recovery),
                new StationAdminFacade(airlock, robot, reporting, chainLog, recovery),
                new DemoCrashSimulator(airlock, robot, chainLog));
    }

    public InspectionFacade inspection() {
        return inspection;
    }

    public StationAdminFacade admin() {
        return admin;
    }

    public DemoCrashSimulator crashSimulator() {
        return crashSimulator;
    }
}