package org.example.airlock;

import org.example.error.IllegalStatusTransitionException;

public class AirlockReservation {

    public enum Status { RESERVED, CONFIRMED, RELEASED }

    private final String id;
    private final String moduleId;
    private Status status;

    public AirlockReservation(String id, String moduleId) {
        this(id, moduleId, Status.RESERVED);
    }

    public AirlockReservation(String id, String moduleId, Status status) {
        this.id = id;
        this.moduleId = moduleId;
        this.status = status;
    }

    public void confirm() {
        moveTo(Status.CONFIRMED);
    }

    public void release() {
        moveTo(Status.RELEASED);
    }

    private void moveTo(Status next) {
        if (status != Status.RESERVED) {
            throw new IllegalStatusTransitionException(
                    "Reservation " + id + ": cannot go from " + status + " to " + next);
        }
        status = next;
    }

    public String getId() { return id; }

    public String getModuleId() { return moduleId; }

    public Status getStatus() { return status; }

    @Override
    public String toString() {
        return id + " [" + moduleId + ", " + status + "]";
    }
}
