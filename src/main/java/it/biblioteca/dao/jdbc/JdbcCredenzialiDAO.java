package it.biblioteca.dao.jdbc;

import it.biblioteca.dao.CredenzialiDAO;
import it.biblioteca.db.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class JdbcCredenzialiDAO implements CredenzialiDAO {

    @Override
    public boolean upsertCredenziali(Long utenteId, String username, String plainPassword, String role) {
        if (utenteId == null || username == null || username.isBlank() || plainPassword == null)
            return false;
        String checkSql = "SELECT id FROM credenziali WHERE utente_id = ?";
        String insertSql = "INSERT INTO credenziali (utente_id, username, password_hash, password_plain, role) VALUES (?,?,?,?,?)";
        String updateSql = "UPDATE credenziali SET username = ?, password_hash = ?, password_plain = ?, role = ? WHERE utente_id = ?";

        try (Connection c = DatabaseConfig.getConnection()) {
            // check exist
            try (PreparedStatement ps = c.prepareStatement(checkSql)) {
                ps.setLong(1, utenteId);
                try (ResultSet rs = ps.executeQuery()) {
                    String hash = sha256(plainPassword);
                    if (rs.next()) {
                        // update
                        try (PreparedStatement up = c.prepareStatement(updateSql)) {
                            up.setString(1, username);
                            up.setString(2, hash);
                            up.setString(3, plainPassword);
                            up.setString(4, role != null ? role : "UTENTE");
                            up.setLong(5, utenteId);
                            return up.executeUpdate() > 0;
                        }
                    } else {
                        // insert
                        try (PreparedStatement ins = c.prepareStatement(insertSql)) {
                            ins.setLong(1, utenteId);
                            ins.setString(2, username);
                            ins.setString(3, sha256(plainPassword));
                            ins.setString(4, plainPassword);
                            ins.setString(5, role != null ? role : "UTENTE");
                            return ins.executeUpdate() > 0;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("JdbcCredenzialiDAO.upsertCredenziali errore: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean rimuoviCredenziali(Long utenteId) {
        if (utenteId == null) return false;
        String sql = "DELETE FROM credenziali WHERE utente_id = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, utenteId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("JdbcCredenzialiDAO.rimuoviCredenziali errore: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String getUsernameForUserId(Long utenteId) {
        if (utenteId == null) return null;
        String sql = "SELECT username FROM credenziali WHERE utente_id = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, utenteId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("username");
            }
        } catch (Exception e) {
            System.err.println("JdbcCredenzialiDAO.getUsernameForUserId errore: " + e.getMessage());
        }
        return null;
    }

    @Override
    public String getPasswordPlainForUserId(Long utenteId) {
        if (utenteId == null) return null;
        String sql = "SELECT password_plain FROM credenziali WHERE utente_id = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, utenteId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("password_plain");
            }
        } catch (Exception e) {
            System.err.println("JdbcCredenzialiDAO.getPasswordPlainForUserId errore: " + e.getMessage());
        }
        return null;
    }

    @Override
    public Long findUserIdByUsername(String username) {
        if (username == null) return null;
        String sql = "SELECT utente_id FROM credenziali WHERE username = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getObject("utente_id") != null ? rs.getLong("utente_id") : null;
                }
            }
        } catch (Exception e) {
            System.err.println("JdbcCredenzialiDAO.findUserIdByUsername errore: " + e.getMessage());
        }
        return null;
    }

    // helper: SHA256
    private static String sha256(String s) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(s.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { return null; }
    }
}
