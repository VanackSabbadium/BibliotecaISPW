package it.biblioteca.security;

import it.biblioteca.db.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public final class CredentialsService {

    private CredentialsService() {}

    public static boolean creaCredenziali(Long utenteId, String username, String passwordPlain, String role) {
        if (utenteId == null || username == null || username.isBlank() || passwordPlain == null || passwordPlain.isBlank())
            return false;
        final String sql = "INSERT INTO credenziali (utente_id, username, password_hash, role) VALUES (?, ?, ?, ?)";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, utenteId);
            ps.setString(2, username);
            ps.setString(3, sha256(passwordPlain));
            ps.setString(4, role != null ? role : "UTENTE");
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("CredentialsService.creaCredenziali failed: " + e.getMessage());
            return false;
        }
    }

    public static boolean aggiornaCredenziali(Long utenteId, String username, String passwordPlain, String role) {
        if (utenteId == null) return false;
        final String sql = "UPDATE credenziali SET username = ?, password_hash = ?, role = ? WHERE utente_id = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, sha256(passwordPlain));
            ps.setString(3, role != null ? role : "UTENTE");
            ps.setLong(4, utenteId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("CredentialsService.aggiornaCredenziali failed: " + e.getMessage());
            return false;
        }
    }

    public static boolean rimuoviCredenziali(Long utenteId) {
        if (utenteId == null) return false;
        final String sql = "DELETE FROM credenziali WHERE utente_id = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, utenteId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("CredentialsService.rimuoviCredenziali failed: " + e.getMessage());
            return false;
        }
    }

    public static CredInfo findByUtenteId(Long utenteId) {
        final String sql = "SELECT username, role FROM credenziali WHERE utente_id = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, utenteId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new CredInfo(rs.getString("username"), rs.getString("role"));
                }
            }
        } catch (Exception e) {
            System.err.println("CredentialsService.findByUtenteId failed: " + e.getMessage());
        }
        return null;
    }

    public static final class CredInfo {
        public final String username;
        public final String role;
        public CredInfo(String username, String role) { this.username = username; this.role = role; }
    }

    private static String sha256(String input) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] b = md.digest(input.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte x : b) sb.append(String.format("%02x", x & 0xff));
            return sb.toString();
        } catch (Exception ex) { throw new RuntimeException(ex); }
    }
}
