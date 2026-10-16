package org.example.chain;

import org.example.airlock.AirlockReservation;
import org.example.airlock.AirlockScheduler;
import org.example.robot.InspectionRobotController;

public class ChainRecovery {
    private final AirlockScheduler airlock;
    private final InspectionRobotController robot;
    private final ChainLogRepository chainLog;

    public ChainRecovery(AirlockScheduler airlock, InspectionRobotController robot, ChainLogRepository chainLog) {
        this.airlock = airlock;
        this.robot = robot;
        this.chainLog = chainLog;
    }

    public void compensate(ChainRecord chain, String reason) {
        chain.beginCompensation(reason);
        chainLog.save(chain);

        if (chain.getRunId() != null) {
            robot.abort(chain.getRunId());
        }
        airlock.release(chain.getReservationId());

        chain.markCompensated();
        chainLog.save(chain);
    }

    public void confirmReservationIfPending(ChainRecord chain) {
        AirlockReservation reservation = airlock.get(chain.getReservationId());
        if (reservation.getStatus() == AirlockReservation.Status.RESERVED) {
            airlock.confirm(reservation.getId());
        }
    }
}