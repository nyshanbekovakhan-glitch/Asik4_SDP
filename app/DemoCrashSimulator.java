package org.example.app;

import org.example.airlock.AirlockReservation;
import org.example.airlock.AirlockScheduler;
import org.example.chain.ChainLogRepository;
import org.example.chain.ChainRecord;
import org.example.robot.InspectionRobotController;
import org.example.robot.InspectionRun;

import java.util.UUID;

public class DemoCrashSimulator {
    private final AirlockScheduler airlock;
    private final InspectionRobotController robot;
    private final ChainLogRepository chainLog;

    public DemoCrashSimulator(AirlockScheduler airlock,
                              InspectionRobotController robot,
                              ChainLogRepository chainLog) {
        this.airlock = airlock;
        this.robot = robot;
        this.chainLog = chainLog;
    }

    public ChainRecord leaveChainWaitingForRobot(String moduleId) {
        AirlockReservation reservation = airlock.reserve(moduleId);
        ChainRecord chain = new ChainRecord(newChainId(), moduleId, reservation.getId());
        chainLog.save(chain);
        return chain;
    }

    public ChainRecord leaveChainWaitingForReport(String moduleId) {
        AirlockReservation reservation = airlock.reserve(moduleId);
        InspectionRun run;
        try {
            run = robot.inspect(moduleId);
        } catch (RuntimeException e) {
            airlock.release(reservation.getId());
            throw e;
        }
        ChainRecord chain = new ChainRecord(newChainId(), moduleId, reservation.getId());
        chain.markBDone(run.getId());
        chainLog.save(chain);
        return chain;
    }

    private String newChainId() {
        return "CH-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
