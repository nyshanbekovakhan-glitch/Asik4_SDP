package org.example.reporting;

import org.example.error.IllegalStatusTransitionException;

public class InspectionReport {

    public enum Status { DRAFT, FILED, VOIDED }

    private final String id;
    private final String moduleId;
    private final int defectCount;
    private final DefectClassifier.Severity severity;
    private Status status;
    private String repairTicketId;

    public InspectionReport(String id, String moduleId, int defectCount, DefectClassifier.Severity severity) {
        this(id, moduleId, defectCount, severity, Status.DRAFT, null);
    }

    public InspectionReport(String id, String moduleId, int defectCount,
                            DefectClassifier.Severity severity, Status status, String repairTicketId) {
        this.id = id;
        this.moduleId = moduleId;
        this.defectCount = defectCount;
        this.severity = severity;
        this.status = status;
        this.repairTicketId = repairTicketId;
    }

    public void file(String repairTicketId) {
        if (status != Status.DRAFT) {
            throw illegal(Status.FILED);
        }
        this.repairTicketId = repairTicketId;
        status = Status.FILED;
    }

    public void voidReport() {
        if (status == Status.VOIDED) {
            throw illegal(Status.VOIDED);
        }
        status = Status.VOIDED;
    }

    private IllegalStatusTransitionException illegal(Status next) {
        return new IllegalStatusTransitionException(
                "Report " + id + ": cannot go from " + status + " to " + next);
    }

    public String getId() { return id; }

    public String getModuleId() { return moduleId; }

    public int getDefectCount() { return defectCount; }

    public DefectClassifier.Severity getSeverity() { return severity; }

    public Status getStatus() { return status; }

    public String getRepairTicketId() { return repairTicketId; }

    @Override
    public String toString() {
        return id + " [" + moduleId + ", " + severity + ", " + status
                + (repairTicketId == null ? "" : ", ticket=" + repairTicketId) + "]";
    }
}