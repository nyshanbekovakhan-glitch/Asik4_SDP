package org.example.error;
public class InspectionNotFoundException extends StationException {
    public InspectionNotFoundException(String id) {
        super("Inspection run not found: " + id);
    }
}
