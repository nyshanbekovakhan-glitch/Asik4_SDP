package org.example.facade;

import org.example.airlock.AirlockReservation;
import org.example.chain.ChainStatus;
import org.example.robot.InspectionRun;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.example.facade.StationScenario.aStation;

class RestartTest {

    @TempDir
    Path tempDir;

    @Test
    void aChainWaitingForTheReportIsStillOnDiskAfterARestart() {
        aStation(tempDir)
                .aCrashLeftTheChainWaitingForReport("MODULE-4")
                .afterRestart()
                .thenTheOnlyChainIs(ChainStatus.PENDING_C)
                .thenTheChainRememberedTheRobotRun()
                .thenTheAirlockReservationIs(AirlockReservation.Status.RESERVED)
                .thenTheRobotRunIs(InspectionRun.Status.COMPLETED);
    }

    @Test
    void recoveryUndoesTheRobotAndThenTheAirlockWhenTheReportWasMissing() {
        aStation(tempDir)
                .aCrashLeftTheChainWaitingForReport("MODULE-4")
                .afterRestart()
                .recoveringAfterRestart()
                .thenRecoveredChains(1)
                .thenTheOnlyChainIs(ChainStatus.COMPENSATED)
                .thenTheRobotRunIs(InspectionRun.Status.ABORTED)
                .thenTheAirlockReservationIs(AirlockReservation.Status.RELEASED);
    }

    @Test
    void recoveryUndoesOnlyTheAirlockWhenTheRobotNeverFinished() {
        aStation(tempDir)
                .aCrashLeftTheChainWaitingForRobot("MODULE-4")
                .afterRestart()
                .thenTheOnlyChainIs(ChainStatus.PENDING_B)
                .recoveringAfterRestart()
                .thenRecoveredChains(1)
                .thenTheOnlyChainIs(ChainStatus.COMPENSATED)
                .thenTheChainHadNoRobotRunRecorded()
                .thenTheAirlockReservationIs(AirlockReservation.Status.RELEASED)
                .thenNoRobotRunExists();
    }

    @Test
    void recoveringTwiceChangesNothingTheSecondTime() {
        aStation(tempDir)
                .aCrashLeftTheChainWaitingForReport("MODULE-4")
                .afterRestart()
                .recoveringAfterRestart()
                .recoveringAfterRestart()
                .thenRecoveredChains(0)
                .thenTheOnlyChainIs(ChainStatus.COMPENSATED);
    }

    @Test
    void recoveryLeavesFinishedChainsAlone() {
        aStation(tempDir)
                .inspecting("MODULE-4")
                .afterRestart()
                .recoveringAfterRestart()
                .thenRecoveredChains(0)
                .thenTheOnlyChainIs(ChainStatus.COMPLETED)
                .thenTheAirlockReservationIs(AirlockReservation.Status.CONFIRMED);
    }
}