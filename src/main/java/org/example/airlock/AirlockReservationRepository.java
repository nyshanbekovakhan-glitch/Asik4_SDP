package org.example.airlock;

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

public class AirlockReservationRepository {
    static final double DEFAULT_RADIATION_LEVEL = 0.2;
    private static final String RADIATION_KEY = "radiation_level";

    private final Database db;

    public AirlockReservationRepository(Database db) {
        this.db = db;
        createTables();
    }

    private void createTables() {
        try (Connection c = db.open(); Statement s = c.createStatement()) {
            s.execute("CREATE TABLE IF NOT EXISTS airlock_reservations ("
                    + "id TEXT PRIMARY KEY, module_id TEXT NOT NULL, status TEXT NOT NULL)");
            s.execute("CREATE TABLE IF NOT EXISTS airlock_env ("
                    + "env_key TEXT PRIMARY KEY, env_value REAL NOT NULL)");
        } catch (SQLException e) {
            throw new StorageException("Cannot create airlock tables", e);
        }
    }

    public void save(AirlockReservation reservation) {
        String sql = "INSERT INTO airlock_reservations (id, module_id, status) VALUES (?, ?, ?) "
                + "ON CONFLICT(id) DO UPDATE SET status = excluded.status";
        try (Connection c = db.open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, reservation.getId());
            ps.setString(2, reservation.getModuleId());
            ps.setString(3, reservation.getStatus().name());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new StorageException("Cannot save reservation " + reservation.getId(), e);
        }
    }

    public Optional<AirlockReservation> findById(String id) {
        String sql = "SELECT id, module_id, status FROM airlock_reservations WHERE id = ?";
        try (Connection c = db.open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(toReservation(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new StorageException("Cannot read reservation " + id, e);
        }
    }

    public List<AirlockReservation> findAll() {
        String sql = "SELECT id, module_id, status FROM airlock_reservations ORDER BY rowid";
        List<AirlockReservation> result = new ArrayList<>();
        try (Connection c = db.open(); Statement s = c.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                result.add(toReservation(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new StorageException("Cannot list reservations", e);
        }
    }

    public int countByStatus(AirlockReservation.Status status) {
        String sql = "SELECT COUNT(*) FROM airlock_reservations WHERE status = ?";
        try (Connection c = db.open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new StorageException("Cannot count reservations", e);
        }
    }

    public double loadRadiationLevel() {
        String sql = "SELECT env_value FROM airlock_env WHERE env_key = ?";
        try (Connection c = db.open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, RADIATION_KEY);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : DEFAULT_RADIATION_LEVEL;
            }
        } catch (SQLException e) {
            throw new StorageException("Cannot read radiation level", e);
        }
    }

    public void saveRadiationLevel(double level) {
        String sql = "INSERT INTO airlock_env (env_key, env_value) VALUES (?, ?) "
                + "ON CONFLICT(env_key) DO UPDATE SET env_value = excluded.env_value";
        try (Connection c = db.open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, RADIATION_KEY);
            ps.setDouble(2, level);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new StorageException("Cannot save radiation level", e);
        }
    }

    private AirlockReservation toReservation(ResultSet rs) throws SQLException {
        return new AirlockReservation(
                rs.getString("id"),
                rs.getString("module_id"),
                AirlockReservation.Status.valueOf(rs.getString("status")));
    }
}
