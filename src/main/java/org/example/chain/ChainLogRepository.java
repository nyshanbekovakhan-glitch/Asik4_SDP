package org.example.chain;

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

public class ChainLogRepository {
    private final Database db;

    public ChainLogRepository(Database db) {
        this.db = db;
        createTable();
    }

    private void createTable() {
        try (Connection c = db.open(); Statement s = c.createStatement()) {
            s.execute("CREATE TABLE IF NOT EXISTS inspection_chain ("
                    + "id TEXT PRIMARY KEY, module_id TEXT NOT NULL, reservation_id TEXT NOT NULL, "
                    + "run_id TEXT, report_id TEXT, status TEXT NOT NULL, note TEXT)");
        } catch (SQLException e) {
            throw new StorageException("Cannot create chain table", e);
        }
    }

    public void save(ChainRecord chain) {
        String sql = "INSERT INTO inspection_chain "
                + "(id, module_id, reservation_id, run_id, report_id, status, note) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?) "
                + "ON CONFLICT(id) DO UPDATE SET run_id = excluded.run_id, "
                + "report_id = excluded.report_id, status = excluded.status, note = excluded.note";
        try (Connection c = db.open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, chain.getId());
            ps.setString(2, chain.getModuleId());
            ps.setString(3, chain.getReservationId());
            ps.setString(4, chain.getRunId());
            ps.setString(5, chain.getReportId());
            ps.setString(6, chain.getStatus().name());
            ps.setString(7, chain.getNote());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new StorageException("Cannot save chain " + chain.getId(), e);
        }
    }

    public Optional<ChainRecord> findById(String id) {
        String sql = "SELECT id, module_id, reservation_id, run_id, report_id, status, note "
                + "FROM inspection_chain WHERE id = ?";
        try (Connection c = db.open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(toRecord(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new StorageException("Cannot read chain " + id, e);
        }
    }

    public List<ChainRecord> findAll() {
        String sql = "SELECT id, module_id, reservation_id, run_id, report_id, status, note "
                + "FROM inspection_chain ORDER BY rowid";
        List<ChainRecord> result = new ArrayList<>();
        try (Connection c = db.open(); Statement s = c.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                result.add(toRecord(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new StorageException("Cannot list chains", e);
        }
    }

    private ChainRecord toRecord(ResultSet rs) throws SQLException {
        return new ChainRecord(
                rs.getString("id"),
                rs.getString("module_id"),
                rs.getString("reservation_id"),
                rs.getString("run_id"),
                rs.getString("report_id"),
                ChainStatus.valueOf(rs.getString("status")),
                rs.getString("note"));
    }
}