package org.example.reporting;

import org.example.error.StorageException;
import org.example.infra.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReportRepository {
    private static final String ONLINE_KEY = "system_online";

    private final Database db;

    public ReportRepository(Database db) {
        this.db = db;
        createTables();
    }

    private void createTables() {
        try (Connection c = db.open(); Statement s = c.createStatement()) {
            s.execute("CREATE TABLE IF NOT EXISTS reports ("
                    + "id TEXT PRIMARY KEY, module_id TEXT NOT NULL, defect_count INTEGER NOT NULL, "
                    + "severity TEXT NOT NULL, status TEXT NOT NULL, ticket_id TEXT)");
            s.execute("CREATE TABLE IF NOT EXISTS report_env ("
                    + "env_key TEXT PRIMARY KEY, env_value REAL NOT NULL)");
        } catch (SQLException e) {
            throw new StorageException("Cannot create report tables", e);
        }
    }

    public void save(InspectionReport report) {
        String sql = "INSERT INTO reports (id, module_id, defect_count, severity, status, ticket_id) "
                + "VALUES (?, ?, ?, ?, ?, ?) "
                + "ON CONFLICT(id) DO UPDATE SET status = excluded.status, ticket_id = excluded.ticket_id";
        try (Connection c = db.open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, report.getId());
            ps.setString(2, report.getModuleId());
            ps.setInt(3, report.getDefectCount());
            ps.setString(4, report.getSeverity().name());
            ps.setString(5, report.getStatus().name());
            ps.setString(6, report.getRepairTicketId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new StorageException("Cannot save report " + report.getId(), e);
        }
    }

    public Optional<InspectionReport> findById(String id) {
        String sql = "SELECT id, module_id, defect_count, severity, status, ticket_id FROM reports WHERE id = ?";
        try (Connection c = db.open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(toReport(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new StorageException("Cannot read report " + id, e);
        }
    }

    public List<InspectionReport> findAll() {
        String sql = "SELECT id, module_id, defect_count, severity, status, ticket_id FROM reports ORDER BY rowid";
        List<InspectionReport> result = new ArrayList<>();
        try (Connection c = db.open(); Statement s = c.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                result.add(toReport(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new StorageException("Cannot list reports", e);
        }
    }

    public boolean loadSystemOnline() {
        String sql = "SELECT env_value FROM report_env WHERE env_key = ?";
        try (Connection c = db.open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, ONLINE_KEY);
            try (ResultSet rs = ps.executeQuery()) {
                return !rs.next() || rs.getDouble(1) == 1.0;
            }
        } catch (SQLException e) {
            throw new StorageException("Cannot read report system state", e);
        }
    }

    public void saveSystemOnline(boolean online) {
        String sql = "INSERT INTO report_env (env_key, env_value) VALUES (?, ?) "
                + "ON CONFLICT(env_key) DO UPDATE SET env_value = excluded.env_value";
        try (Connection c = db.open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, ONLINE_KEY);
            ps.setDouble(2, online ? 1.0 : 0.0);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new StorageException("Cannot save report system state", e);
        }
    }

    private InspectionReport toReport(ResultSet rs) throws SQLException {
        return new InspectionReport(
                rs.getString("id"),
                rs.getString("module_id"),
                rs.getInt("defect_count"),
                DefectClassifier.Severity.valueOf(rs.getString("severity")),
                InspectionReport.Status.valueOf(rs.getString("status")),
                rs.getString("ticket_id"));
    }
}