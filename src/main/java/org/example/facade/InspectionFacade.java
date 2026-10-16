package org.example.facade;

import org.example.airlock.AirlockReservation;
import org.example.airlock.AirlockScheduler;
import org.example.chain.ChainLogRepository;
import org.example.chain.ChainRecord;
import org.example.chain.ChainRecovery;
import org.example.reporting.InspectionReport;
import org.example.reporting.ReportDesk;
import org.example.robot.InspectionRobotController;
import org.example.robot.InspectionRun;

import java.util.UUID;

public class InspectionFacade {
    private final AirlockScheduler airlock;
    private final InspectionRobotController robot;
    private final ReportDesk reporting;
    private final ChainLogRepository chainLog;
    private final ChainRecovery recovery;

    public InspectionFacade(AirlockScheduler airlock,
                            InspectionRobotController robot,
                            ReportDesk reporting,
                            ChainLogRepository chainLog,
                            ChainRecovery recovery) {
        this.airlock = airlock;
        this.robot = robot;
        this.reporting = reporting;
        this.chainLog = chainLog;
        this.recovery = recovery;
    }

    public InspectionResult runInspection(String moduleId) {
        AirlockReservation reservation = airlock.reserve(moduleId);
        ChainRecord chain = new ChainRecord(
                "CH-" + UUID.randomUUID().toString().substring(0, 8), moduleId, reservation.getId());
        chainLog.save(chain);

        InspectionRun run;
        try {
            run = robot.inspect(moduleId);
        } catch (RuntimeException e) {
            recovery.compensate(chain, e.getMessage());
            throw e;
        }
        chain.markBDone(run.getId());
        chainLog.save(chain);

        InspectionReport report;
        try {
            report = reporting.fileReport(moduleId, run.getDefectCount());
        } catch (RuntimeException e) {
            recovery.compensate(chain, e.getMessage());
            throw e;
        }
        chain.markCompleted(report.getId());
        chainLog.save(chain);
        airlock.confirm(reservation.getId());

        return new InspectionResult(chain.getId(), moduleId, report.getDefectCount(),
                report.getSeverity().name(), report.getRepairTicketId());
    }
}