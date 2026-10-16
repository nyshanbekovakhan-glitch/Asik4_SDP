package org.example.error;
public class ReportingUnavailableException extends StationException {
    public ReportingUnavailableException() {
        super("Report system is offline");
    }
}
