package org.example.robot;

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

public class InspectionRunRepository {
    private static final String SIGNAL_KEY = "signal_online";
    private static final String BATTERY_KEY = "battery";

    private final Database db;

    public InspectionRunRepository(Database db) {
        this.db = db;
        createTables();
    }

    private void createTables() {
        try (Connection c = db.open(); Statement s = c.createStatement()) {
            s.execute("CREATE TABLE IF NOT EXISTS inspection_runs ("
                    + "id TEXT PRIMARY KEY, module_id TEXT NOT NULL, "
                    + "status TEXT NOT NULL, defect_count INTEGER NOT NULL)");
            s.execute("CREATE TABLE IF NOT EXISTS robot_env ("
                    + "env_key TEXT PRIMARY KEY, env_value REAL NOT NULL)");
        } catch (SQLException e) {
            throw new StorageException("Cannot create robot tables", e);
        }
    }

    public void save(InspectionRun run) {
        String sql = "INSERT INTO inspection_runs (id, module_id, status, defect_count) VALUES (?, ?, ?, ?) "
                + "ON CONFLICT(id) DO UPDATE SET status = excluded.status, defect_count = excluded.defect_count";
        try (Connection c = db.open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, run.getId());
            ps.setString(2, run.getModuleId());
            ps.setString(3, run.getStatus().name());
            ps.setInt(4, run.getDefectCount());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new StorageException("Cannot save run " + run.getId(), e);
        }
    }

    public Optional<InspectionRun> findById(String id) {
        String sql = "SELECT id, module_id, status, defect_count FROM inspection_runs WHERE id = ?";
        try (Connection c = db.open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(toRun(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new StorageException("Cannot read run " + id, e);
        }
    }

    public List<InspectionRun> findAll() {
        String sql = "SELECT id, module_id, status, defect_count FROM inspection_runs ORDER BY rowid";
        List<InspectionRun> result = new ArrayList<>();
        try (Connection c = db.open(); Statement s = c.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                result.add(toRun(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new StorageException("Cannot list runs", e);
        }
    }

    public boolean loadSignalOnline() {
        return loadEnv(SIGNAL_KEY, 1.0) == 1.0;
    }

    public void saveSignalOnline(boolean online) {
        saveEnv(SIGNAL_KEY, online ? 1.0 : 0.0);
    }

    public int loadBattery() {
        return (int) loadEnv(BATTERY_KEY, 100);
    }

    public void saveBattery(int level) {
        saveEnv(BATTERY_KEY, level);
    }

    private double loadEnv(String key, double fallback) {
        String sql = "SELECT env_value FROM robot_env WHERE env_key = ?";
        try (Connection c = db.open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : fallback;
            }
        } catch (SQLException e) {
            throw new StorageException("Cannot read robot setting " + key, e);
        }
    }

    private void saveEnv(String key, double value) {
        String sql = "INSERT INTO robot_env (env_key, env_value) VALUES (?, ?) "
                + "ON CONFLICT(env_key) DO UPDATE SET env_value = excluded.env_value";
        try (Connection c = db.open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setDouble(2, value);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new StorageException("Cannot save robot setting " + key, e);
        }
    }

    private InspectionRun toRun(ResultSet rs) throws SQLException {
        return new InspectionRun(
                rs.getString("id"),
                rs.getString("module_id"),
                InspectionRun.Status.valueOf(rs.getString("status")),
                rs.getInt("defect_count"));
    }
}