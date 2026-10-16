package org.example.chain;

public enum ChainStatus {
    PENDING_B,
    PENDING_C,
    COMPLETED,
    COMPENSATING,
    COMPENSATED;

    public boolean isUnfinished() {
        return this == PENDING_B || this == PENDING_C || this == COMPENSATING;
    }
}