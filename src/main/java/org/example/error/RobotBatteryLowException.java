package org.example.error;
public class RobotBatteryLowException extends StationException {
    public RobotBatteryLowException(int battery, int minimum) {
        super("Robot battery is " + battery + "%, at least " + minimum + "% is required");
    }
}
