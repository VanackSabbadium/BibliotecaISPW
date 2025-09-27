package it.biblioteca.security;

import it.biblioteca.db.DatabaseConfig;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public final class AuthService {

    public record AuthResult(boolean ok, String username, SessionContext.AppRole role, Long userId, Integer tessera) {
    }

    public static AuthResult authenticate(String username, String passwordPlain) {
        if (username == null || username.isBlank() || passwordPlain == null) {
            return new AuthResult(false, null, null, null, null);
        }

        final String sql = "SELECT id, utente_id, username, password_hash, role FROM credenziali WHERE username = ?";

        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return new AuthResult(false, null, null, null, null);

                String storedHash = rs.getString("password_hash");
                Long utenteId = rs.getObject("utente_id") != null ? rs.getLong("utente_id") : null;
                String roleStr = rs.getString("role");

                if (storedHash == null || !storedHash.equals(sha256(passwordPlain))) {
                    return new AuthResult(false, null, null, null, null);
                }

                SessionContext.AppRole role = switch ((roleStr != null ? roleStr : "UTENTE")) {
                    case "ADMIN" -> SessionContext.AppRole.ADMIN;
                    case "BIBLIOTECARIO" -> SessionContext.AppRole.BIBLIOTECARIO;
                    default -> SessionContext.AppRole.UTENTE;
                };

                Integer tessera = null;
                if (utenteId != null) {
                    try (PreparedStatement ps2 = c.prepareStatement("SELECT tessera FROM utenti WHERE id = ?")) {
                        ps2.setLong(1, utenteId);
                        try (ResultSet rs2 = ps2.executeQuery()) {
                            if (rs2.next()) {
                                Object t = rs2.getObject("tessera");
                                if (t != null) tessera = ((Number) t).intValue();
                            }
                        }
                    } catch (Exception ignore) {}
                }

                // set session
                SessionContext.setRole(role);
                SessionContext.setAuthenticatedUsername(username);
                SessionContext.setUserId(utenteId);
                if (tessera != null) SessionContext.setTessera(tessera);

                return new AuthResult(true, username, role, utenteId, tessera);
            }
        } catch (Exception e) {
            System.err.println("AuthService.authenticate() errore: " + e.getMessage());
            return new AuthResult(false, null, null, null, null);
        }
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] b = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte x : b) sb.append(String.format("%02x", x & 0xff));
            return sb.toString();
        } catch (Exception ex) { throw new RuntimeException(ex); }
    }
}
