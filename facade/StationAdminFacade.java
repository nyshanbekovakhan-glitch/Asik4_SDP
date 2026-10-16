package org.example.facade;

import org.example.airlock.AirlockReservation;
import org.example.airlock.AirlockScheduler;
import org.example.chain.ChainLogRepository;
import org.example.chain.ChainRecord;
import org.example.chain.ChainRecovery;
import org.example.chain.ChainStatus;
import org.example.error.ChainNotFoundException;
import org.example.reporting.InspectionReport;
import org.example.reporting.ReportDesk;
import org.example.robot.InspectionRobotController;
import org.example.robot.InspectionRun;

import java.util.List;

public class StationAdminFacade {
    private final AirlockScheduler airlock;
    private final InspectionRobotController robot;
    private final ReportDesk reporting;
    private final ChainLogRepository chainLog;
    private final ChainRecovery recovery;

    public StationAdminFacade(AirlockScheduler airlock,
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

    public List<ChainRecord> chains() {
        return chainLog.findAll();
    }

    public List<ChainRecord> unfinishedChains() {
        return chainLog.findAll().stream()
                .filter(chain -> chain.getStatus().isUnfinished())
                .toList();
    }

    public int recoverAfterRestart() {
        int recovered = 0;
        for (ChainRecord chain : unfinishedChains()) {
            recovery.compensate(chain, "recovered after restart");
            recovered++;
        }
        for (ChainRecord chain : chainLog.findAll()) {
            if (chain.getStatus() == ChainStatus.COMPLETED) {
                recovery.confirmReservationIfPending(chain);
            }
        }
        return recovered;
    }

    public ChainRecord compensateManually(String chainId) {
        ChainRecord chain = chainLog.findById(chainId)
                .orElseThrow(() -> new ChainNotFoundException(chainId));
        recovery.compensate(chain, "manual compensation");
        return chain;
    }

    public List<AirlockReservation> reservations() {
        return airlock.all();
    }

    public List<InspectionRun> runs() {
        return robot.all();
    }

    public List<InspectionReport> reports() {
        return reporting.all();
    }

    public void setRadiationLevel(double level) {
        airlock.setRadiationLevel(level);
    }

    public void setRobotSignal(boolean online) {
        robot.setSignalOnline(online);
    }

    public void setRobotBattery(int level) {
        robot.setBattery(level);
    }

    public void setReportSystemOnline(boolean online) {
        reporting.setSystemOnline(online);
    }
    public String environmentSummary() {
        return "radiation=" + airlock.radiationLevel() + " mSv/h"
                + ", robot signal=" + (robot.isSignalOnline() ? "ON" : "OFF")
                + ", robot battery=" + robot.battery() + "%"
                + ", report system=" + (reporting.isSystemOnline() ? "ON" : "OFF");
    }
}