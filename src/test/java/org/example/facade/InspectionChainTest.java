package org.example.facade;

import org.example.airlock.AirlockReservation;
import org.example.chain.ChainStatus;
import org.example.error.InvalidModuleException;
import org.example.error.ReportingUnavailableException;
import org.example.error.RobotLinkLostException;
import org.example.error.UnsafeRadiationException;
import org.example.reporting.InspectionReport;
import org.example.robot.InspectionRun;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.example.facade.StationScenario.aStation;

class InspectionChainTest {

    @TempDir
    Path tempDir;

    @Test
    void aSuccessfulInspectionCompletesTheWholeChain() {
        aStation(tempDir)
                .inspecting("MODULE-4")
                .thenTheInspectionSucceeded()
                .thenTheOnlyChainIs(ChainStatus.COMPLETED)
                .thenTheAirlockReservationIs(AirlockReservation.Status.CONFIRMED)
                .thenTheRobotRunIs(InspectionRun.Status.COMPLETED)
                .thenTheReportIs(InspectionReport.Status.FILED);
    }

    @Test
    void whenTheRobotFailsOnlyTheAirlockIsCompensated() {
        aStation(tempDir)
                .robotLosesSignal()
                .inspecting("MODULE-5")
                .thenTheInspectionFailedWith(RobotLinkLostException.class)
                .thenTheOnlyChainIs(ChainStatus.COMPENSATED)
                .thenTheChainHadNoRobotRunRecorded()
                .thenTheAirlockReservationIs(AirlockReservation.Status.RELEASED)
                .thenTheRobotRunIs(InspectionRun.Status.ABORTED)
                .thenNoReportExists();
    }

    @Test
    void whenTheReportFailsTheRobotAndThenTheAirlockAreCompensated() {
        aStation(tempDir)
                .reportSystemGoesOffline()
                .inspecting("MODULE-7")
                .thenTheInspectionFailedWith(ReportingUnavailableException.class)
                .thenTheOnlyChainIs(ChainStatus.COMPENSATED)
                .thenTheChainRememberedTheRobotRun()
                .thenTheRobotRunIs(InspectionRun.Status.ABORTED)
                .thenTheAirlockReservationIs(AirlockReservation.Status.RELEASED)
                .thenTheReportIs(InspectionReport.Status.VOIDED);
    }

    @Test
    void unsafeRadiationStopsBeforeAnythingIsSaved() {
        aStation(tempDir)
                .radiationRisesTo(5.0)
                .inspecting("MODULE-8")
                .thenTheInspectionFailedWith(UnsafeRadiationException.class)
                .thenNothingWasSavedAnywhere();
    }

    @Test
    void anInvalidModuleIdStopsBeforeAnythingIsSaved() {
        aStation(tempDir)
                .inspecting("abc")
                .thenTheInspectionFailedWith(InvalidModuleException.class)
                .thenNothingWasSavedAnywhere();
    }
}