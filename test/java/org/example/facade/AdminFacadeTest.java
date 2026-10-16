package org.example.facade;

import org.example.airlock.AirlockReservation;
import org.example.chain.ChainStatus;
import org.example.robot.InspectionRun;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.example.facade.StationScenario.aStation;

class AdminFacadeTest {

    @TempDir
    Path tempDir;

    @Test
    void anAdministratorCanCompensateAStuckChainByHand() {
        aStation(tempDir)
                .aCrashLeftTheChainWaitingForReport("MODULE-4")
                .manuallyCompensatingTheOnlyChain()
                .thenTheOnlyChainIs(ChainStatus.COMPENSATED)
                .thenTheRobotRunIs(InspectionRun.Status.ABORTED)
                .thenTheAirlockReservationIs(AirlockReservation.Status.RELEASED);
    }

    @Test
    void aCompletedChainCannotBeCompensated() {
        aStation(tempDir)
                .inspecting("MODULE-4")
                .thenManualCompensationIsRefused()
                .thenTheOnlyChainIs(ChainStatus.COMPLETED);
    }
}