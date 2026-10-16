package org.example.reporting;

import org.example.error.ReportNotFoundException;
import org.example.error.ReportingUnavailableException;
import org.example.infra.Database;
import org.example.reporting.InspectionReport.Status;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportDeskTest {

    @TempDir
    Path tempDir;

    private ReportDesk aDesk() {
        Database db = new Database(tempDir.resolve("station.db").toString());
        return new ReportDesk(new ReportRepository(db), new DefectClassifier());
    }

    private ReportDesk theSameDeskAfterRestart() {
        return aDesk();
    }

    @Test
    void aReportWithDefectsOpensARepairTicketAndIsStored() {
        InspectionReport report = aDesk().fileReport("MODULE-4", 1);

        InspectionReport onDisk = theSameDeskAfterRestart().get(report.getId());
        assertEquals(Status.FILED, onDisk.getStatus());
        assertEquals(DefectClassifier.Severity.MINOR, onDisk.getSeverity());
        assertNotNull(onDisk.getRepairTicketId());
    }

    @Test
    void aCleanModuleGetsAFiledReportWithoutATicket() {
        InspectionReport report = aDesk().fileReport("MODULE-3", 0);

        assertEquals(Status.FILED, report.getStatus());
        assertNull(report.getRepairTicketId());
    }

    @Test
    void whenTheSystemIsOfflineTheReportIsVoidedAndTheCallFails() {
        ReportDesk desk = aDesk();
        desk.setSystemOnline(false);

        assertThrows(ReportingUnavailableException.class, () -> desk.fileReport("MODULE-5", 2));

        assertEquals(1, desk.all().size());
        assertEquals(Status.VOIDED, desk.all().get(0).getStatus());
    }

    @Test
    void theOnlineStateSurvivesARestart() {
        ReportDesk desk = aDesk();
        assertTrue(desk.isSystemOnline());

        desk.setSystemOnline(false);

        assertFalse(theSameDeskAfterRestart().isSystemOnline());
    }

    @Test
    void aFiledReportCanBeVoidedAndVoidingTwiceIsHarmless() {
        ReportDesk desk = aDesk();
        InspectionReport report = desk.fileReport("MODULE-4", 1);

        desk.voidReport(report.getId());
        desk.voidReport(report.getId());

        assertEquals(Status.VOIDED, desk.get(report.getId()).getStatus());
    }

    @Test
    void anUnknownReportIdIsReported() {
        assertThrows(ReportNotFoundException.class, () -> aDesk().get("REP-nope"));
    }

    @Test
    void aNegativeDefectCountIsRejectedAndNothingIsSaved() {
        ReportDesk desk = aDesk();

        assertThrows(IllegalArgumentException.class, () -> desk.fileReport("MODULE-4", -1));

        assertTrue(desk.all().isEmpty());
    }
}