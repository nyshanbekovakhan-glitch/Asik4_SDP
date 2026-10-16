package org.example.chain;

import org.example.error.IllegalStatusTransitionException;

public class ChainRecord {
    private final String id;
    private final String moduleId;
    private final String reservationId;
    private String runId;
    private String reportId;
    private ChainStatus status;
    private String note;

    public ChainRecord(String id, String moduleId, String reservationId) {
        this(id, moduleId, reservationId, null, null, ChainStatus.PENDING_B, null);
    }

    public ChainRecord(String id, String moduleId, String reservationId,
                       String runId, String reportId, ChainStatus status, String note) {
        this.id = id;
        this.moduleId = moduleId;
        this.reservationId = reservationId;
        this.runId = runId;
        this.reportId = reportId;
        this.status = status;
        this.note = note;
    }

    public void markBDone(String runId) {
        if (status != ChainStatus.PENDING_B) {
            throw illegal(ChainStatus.PENDING_C);
        }
        this.runId = runId;
        status = ChainStatus.PENDING_C;
    }

    public void markCompleted(String reportId) {
        if (status != ChainStatus.PENDING_C) {
            throw illegal(ChainStatus.COMPLETED);
        }
        this.reportId = reportId;
        status = ChainStatus.COMPLETED;
    }

    public void beginCompensation(String reason) {
        if (status == ChainStatus.COMPLETED || status == ChainStatus.COMPENSATED) {
            throw illegal(ChainStatus.COMPENSATING);
        }
        if (status != ChainStatus.COMPENSATING) {
            note = reason;
            status = ChainStatus.COMPENSATING;
        }
    }

    public void markCompensated() {
        if (status != ChainStatus.COMPENSATING) {
            throw illegal(ChainStatus.COMPENSATED);
        }
        status = ChainStatus.COMPENSATED;
    }

    private IllegalStatusTransitionException illegal(ChainStatus next) {
        return new IllegalStatusTransitionException(
                "Chain " + id + ": cannot go from " + status + " to " + next);
    }

    public String getId() { return id; }

    public String getModuleId() { return moduleId; }

    public String getReservationId() { return reservationId; }

    public String getRunId() { return runId; }

    public String getReportId() { return reportId; }

    public ChainStatus getStatus() { return status; }

    public String getNote() { return note; }

    @Override
    public String toString() {
        return id + " [" + moduleId + ", " + status
                + ", A=" + reservationId
                + ", B=" + runId
                + ", C=" + reportId
                + (note == null ? "" : ", note=" + note) + "]";
    }
}