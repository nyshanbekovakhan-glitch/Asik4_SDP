package org.example.reporting;

import org.example.error.ReportNotFoundException;
import org.example.error.ReportingUnavailableException;

import java.util.List;
import java.util.UUID;

public class ReportDesk {
    private final ReportRepository repository;
    private final DefectClassifier classifier;

    public ReportDesk(ReportRepository repository, DefectClassifier classifier) {
        this.repository = repository;
        this.classifier = classifier;
    }

    public InspectionReport fileReport(String moduleId, int defectCount) {
        InspectionReport report = new InspectionReport(
                "REP-" + UUID.randomUUID().toString().substring(0, 8),
                moduleId,
                defectCount,
                classifier.classify(defectCount));
        repository.save(report);

        if (!repository.loadSystemOnline()) {
            report.voidReport();
            repository.save(report);
            throw new ReportingUnavailableException();
        }

        String ticketId = classifier.needsRepairTicket(report.getSeverity())
                ? "TKT-" + UUID.randomUUID().toString().substring(0, 8)
                : null;
        report.file(ticketId);
        repository.save(report);
        return report;
    }

    public InspectionReport voidReport(String reportId) {
        InspectionReport report = get(reportId);
        if (report.getStatus() == InspectionReport.Status.VOIDED) {
            return report;
        }
        report.voidReport();
        repository.save(report);
        return report;
    }

    public InspectionReport get(String reportId) {
        return repository.findById(reportId).orElseThrow(() -> new ReportNotFoundException(reportId));
    }

    public List<InspectionReport> all() {
        return repository.findAll();
    }

    public boolean isSystemOnline() {
        return repository.loadSystemOnline();
    }

    public void setSystemOnline(boolean online) {
        repository.saveSystemOnline(online);
    }
}