package it.biblioteca.security;

import it.biblioteca.db.DatabaseConfig;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class AuthService {

    public record AuthResult(boolean ok, String username, SessionContext.AppRole role, Long userId, Integer tessera) {
    }

    public static AuthResult authenticate(String username, String passwordPlain) {
        if (username == null || username.isBlank() || passwordPlain == null) return fail();
        try (Connection c = DatabaseConfig.getConnection()) {
            CredRow row = loadCredentials(c, username);
            if (row == null || !passwordMatches(passwordPlain, row.storedHash)) return fail();

            SessionContext.AppRole role = toRole(row.roleStr);
            Integer tessera = row.utenteId != null ? fetchTessera(c, row.utenteId) : null;

            SessionContext.setRole(role);
            SessionContext.setAuthenticatedUsername(username);
            SessionContext.setUserId(row.utenteId);
            if (tessera != null) SessionContext.setTessera(tessera);

            return new AuthResult(true, username, role, row.utenteId, tessera);
        } catch (Exception e) {
            System.err.println("AuthService.authenticate() errore: " + e.getMessage());
            return fail();
        }
    }

    private static AuthResult fail() {
        return new AuthResult(false, null, null, null, null);
    }

    private static boolean passwordMatches(String plain, String storedHash) {
        return storedHash != null && storedHash.equals(sha256(plain));
    }

    private static SessionContext.AppRole toRole(String roleStr) {
        if ("ADMIN".equalsIgnoreCase(roleStr)) return SessionContext.AppRole.ADMIN;
        if ("BIBLIOTECARIO".equalsIgnoreCase(roleStr)) return SessionContext.AppRole.BIBLIOTECARIO;
        return SessionContext.AppRole.UTENTE;
    }

    private static Integer fetchTessera(Connection c, Long utenteId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT tessera FROM utenti WHERE id = ?")) {
            ps.setLong(1, utenteId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Object t = rs.getObject("tessera");
                    return t != null ? ((Number) t).intValue() : null;
                }
            }
        }
        return null;
    }

    private static CredRow loadCredentials(Connection c, String username) throws SQLException {
        String sql = "SELECT utente_id, password_hash, role FROM credenziali WHERE username = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                CredRow r = new CredRow();
                r.utenteId = rs.getObject("utente_id") != null ? rs.getLong("utente_id") : null;
                r.storedHash = rs.getString("password_hash");
                r.roleStr = rs.getString("role");
                return r;
            }
        }
    }

    private static final class CredRow {
        Long utenteId;
        String storedHash;
        String roleStr;
    }


    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] b = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte x : b) sb.append(String.format("%02x", x & 0xff));
            return sb.toString();
        } catch (Exception ex) { throw new IllegalArgumentException(ex); }
    }
}
