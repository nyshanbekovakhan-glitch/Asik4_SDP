package org.example.robot;

import org.example.error.IllegalStatusTransitionException;

public class InspectionRun {

    public enum Status { QUEUED, RUNNING, COMPLETED, ABORTED }

    private final String id;
    private final String moduleId;
    private Status status;
    private int defectCount;

    public InspectionRun(String id, String moduleId) {
        this(id, moduleId, Status.QUEUED, 0);
    }

    public InspectionRun(String id, String moduleId, Status status, int defectCount) {
        this.id = id;
        this.moduleId = moduleId;
        this.status = status;
        this.defectCount = defectCount;
    }

    public void start() {
        if (status != Status.QUEUED) {
            throw illegal(Status.RUNNING);
        }
        status = Status.RUNNING;
    }

    public void complete(int defects) {
        if (status != Status.RUNNING) {
            throw illegal(Status.COMPLETED);
        }
        defectCount = defects;
        status = Status.COMPLETED;
    }

    public void abort() {
        if (status == Status.ABORTED) {
            throw illegal(Status.ABORTED);
        }
        defectCount = 0;
        status = Status.ABORTED;
    }

    private IllegalStatusTransitionException illegal(Status next) {
        return new IllegalStatusTransitionException(
                "Run " + id + ": cannot go from " + status + " to " + next);
    }

    public String getId() { return id; }

    public String getModuleId() { return moduleId; }

    public Status getStatus() { return status; }

    public int getDefectCount() { return defectCount; }

    @Override
    public String toString() {
        return id + " [" + moduleId + ", " + status + ", defects=" + defectCount + "]";
    }
}