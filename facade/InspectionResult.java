package org.example.facade;

public record InspectionResult(String chainId, String moduleId, int defectCount,
                               String severity, String repairTicketId) {
}