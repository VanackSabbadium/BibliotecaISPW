package it.biblioteca.auth;

import it.biblioteca.db.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * AuthService autentica contro la tabella credenziali (username,password_hash) usando la connection di servizio.
 * Restituisce utente_id, role e tessera (se presenti).
 */
public final class AuthService {

    private AuthService() {}

    public static class AuthResult {
        private final Long userId;
        private final String role;
        private final Integer tessera;

        public AuthResult(Long userId, String role, Integer tessera) {
            this.userId = userId;
            this.role = role;
            this.tessera = tessera;
        }

        public Long getUserId() { return userId; }
        public String getRole() { return role; }
        public Integer getTessera() { return tessera; }
    }

    public static AuthResult authenticate(String username, String passwordPlain) {
        if (username == null || username.isBlank() || passwordPlain == null) return null;

        final String sql = "SELECT c.utente_id, c.role, u.tessera " +
                "FROM credenziali c LEFT JOIN utenti u ON u.id = c.utente_id " +
                "WHERE c.username = ? AND c.password_hash = SHA2(?,256)";

        try (Connection c = DatabaseConfig.getServiceConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, passwordPlain);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Long uid = rs.getObject("utente_id") != null ? ((Number) rs.getObject("utente_id")).longValue() : null;
                Integer tess = rs.getObject("tessera") != null ? ((Number) rs.getObject("tessera")).intValue() : null;
                String role = rs.getString("role");
                if (role == null) role = "UTENTE";
                return new AuthResult(uid, role, tess);
            }
        } catch (Exception e) {
            System.err.println("AuthService.authenticate() errore: " + e.getMessage());
            return null;
        }
    }
}
