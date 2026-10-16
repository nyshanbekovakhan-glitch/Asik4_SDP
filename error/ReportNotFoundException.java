package org.example.error;
public class ReportNotFoundException extends StationException {
    public ReportNotFoundException(String id) {
        super("Report not found: " + id);
    }
}
