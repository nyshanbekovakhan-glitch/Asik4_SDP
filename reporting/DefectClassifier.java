package org.example.reporting;

public class DefectClassifier {

    public enum Severity { NONE, MINOR, MAJOR, CRITICAL }

    public Severity classify(int defectCount) {
        if (defectCount < 0) {
            throw new IllegalArgumentException("Defect count cannot be negative");
        }
        if (defectCount == 0) {
            return Severity.NONE;
        }
        if (defectCount == 1) {
            return Severity.MINOR;
        }
        if (defectCount == 2) {
            return Severity.MAJOR;
        }
        return Severity.CRITICAL;
    }

    public boolean needsRepairTicket(Severity severity) {
        return severity != Severity.NONE;
    }
}