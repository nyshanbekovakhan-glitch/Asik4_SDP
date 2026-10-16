package org.example.reporting;

import org.example.error.IllegalStatusTransitionException;
import org.example.reporting.InspectionReport.Status;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InspectionReportTest {

    private InspectionReport aDraft() {
        return new InspectionReport("REP-1", "MODULE-4", 1, DefectClassifier.Severity.MINOR);
    }

    @Test
    void newReportIsADraftWithoutATicket() {
        InspectionReport report = aDraft();
        assertEquals(Status.DRAFT, report.getStatus());
        assertNull(report.getRepairTicketId());
    }

    @Test
    void filingADraftKeepsTheRepairTicket() {
        InspectionReport report = aDraft();
        report.file("TKT-1");
        assertEquals(Status.FILED, report.getStatus());
        assertEquals("TKT-1", report.getRepairTicketId());
    }

    @Test
    void aReportCannotBeFiledTwice() {
        InspectionReport report = aDraft();
        report.file("TKT-1");
        assertThrows(IllegalStatusTransitionException.class, () -> report.file("TKT-2"));
    }

    @Test
    void draftAndFiledReportsCanBeVoided() {
        InspectionReport draft = aDraft();
        draft.voidReport();
        assertEquals(Status.VOIDED, draft.getStatus());

        InspectionReport filed = aDraft();
        filed.file(null);
        filed.voidReport();
        assertEquals(Status.VOIDED, filed.getStatus());
    }

    @Test
    void aVoidedReportCannotBeVoidedOrFiledAgain() {
        InspectionReport report = aDraft();
        report.voidReport();
        assertThrows(IllegalStatusTransitionException.class, report::voidReport);
        assertThrows(IllegalStatusTransitionException.class, () -> report.file("TKT-1"));
    }

    @Test
    void toStringMentionsTheTicketOnlyWhenThereIsOne() {
        InspectionReport report = aDraft();
        assertEquals("REP-1 [MODULE-4, MINOR, DRAFT]", report.toString());
        report.file("TKT-1");
        assertEquals("REP-1 [MODULE-4, MINOR, FILED, ticket=TKT-1]", report.toString());
    }
}