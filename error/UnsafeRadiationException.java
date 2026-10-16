package org.example.error;
public class UnsafeRadiationException extends StationException {
    public UnsafeRadiationException(double level, double limit) {
        super("Radiation " + level + " mSv/h is above the safe limit of " + limit + " mSv/h");
    }
}
