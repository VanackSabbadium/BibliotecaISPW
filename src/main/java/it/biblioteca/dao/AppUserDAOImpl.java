package it.biblioteca.dao;

import it.biblioteca.entity.AppUser;

import java.sql.*;

public class AppUserDAOImpl implements AppUserDAO {
    private final ConnectionProvider cp;

    public AppUserDAOImpl() {
        this(new DatabaseConnectionProvider());
    }

    public AppUserDAOImpl(ConnectionProvider cp) {
        this.cp = cp;
    }

    @Override
    public AppUser findByUsername(String username) {
        String sql = "SELECT id, utente_id, username, password_hash, role, created_by, created_at FROM app_users WHERE username = ?";
        try (Connection c = cp.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore findByUsername app_users", e);
        }
        return null;
    }

    @Override
    public AppUser findByUtenteId(Long utenteId) {
        if (utenteId == null) return null;
        String sql = "SELECT id, utente_id, username, password_hash, role, created_by, created_at FROM app_users WHERE utente_id = ?";
        try (Connection c = cp.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, utenteId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore findByUtenteId app_users", e);
        }
        return null;
    }

    @Override
    public boolean insertAppUser(AppUser u) {
        String sql = "INSERT INTO app_users (utente_id, username, password_hash, role, created_by) VALUES (?,?,?,?,?)";
        try (Connection c = cp.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (u.getUtenteId() != null) ps.setLong(1, u.getUtenteId()); else ps.setNull(1, Types.BIGINT);
            ps.setString(2, u.getUsername());
            ps.setString(3, u.getPasswordHash());
            ps.setString(4, u.getRole());
            ps.setString(5, u.getCreatedBy());
            int n = ps.executeUpdate();
            if (n > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) u.setId(keys.getLong(1));
                }
                return true;
            }
        } catch (SQLException e) {
            return false;
        }
        return false;
    }

    @Override
    public boolean usernameExists(String username) {
        String sql = "SELECT 1 FROM app_users WHERE username = ?";
        try (Connection c = cp.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    private AppUser map(ResultSet rs) throws SQLException {
        AppUser u = new AppUser();
        u.setId(rs.getLong("id"));
        Object o = rs.getObject("utente_id");
        u.setUtenteId(o != null ? ((Number)o).longValue() : null);
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRole(rs.getString("role"));
        u.setCreatedBy(rs.getString("created_by"));
        u.setCreatedAt(rs.getTimestamp("created_at"));
        return u;
    }
}
