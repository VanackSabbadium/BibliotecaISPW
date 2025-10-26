package it.biblioteca.security;

import it.biblioteca.dao.DaoFactory;
import it.biblioteca.dao.UtenteDAO;
import it.biblioteca.security.SessionContext.AppRole;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class AuthService {

    private static final Logger LOGGER = Logger.getLogger(AuthService.class.getName());

    private AuthService() { }

    public static void init(DaoFactory factory) {
        SessionContext.setDaoFactory(factory);
    }

    public record AuthResult(
            boolean ok,
            AppRole role,
            Long userId,
            Integer tessera
    ) {
        public static AuthResult fail() { return new AuthResult(false, null, null, null); }
    }

    public static AuthResult authenticate(String username, String passwordPlain) {
        if (isBlank(username) || isBlank(passwordPlain)) return AuthResult.fail();

        try {
            DaoFactory factory = SessionContext.getDaoFactory();
            if (factory == null) {
                LOGGER.warning("AuthService.authenticate(): DaoFactory non inizializzata");
                return AuthResult.fail();
            }

            UtenteDAO utenteDAO = factory.utenteDAO();
            Optional<UtenteDAO.AuthData> rowOpt = utenteDAO.findAuthByUsername(username);
            if (rowOpt.isEmpty()) return AuthResult.fail();

            UtenteDAO.AuthData row = rowOpt.get();
            if (!passwordMatches(passwordPlain, row.passwordHash())) return AuthResult.fail();

            AppRole roleEnum = AppRole.fromDbRole(row.role());
            if (roleEnum == null) return AuthResult.fail();

            return new AuthResult(true, roleEnum, row.userId(), row.tessera());
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore durante l'autenticazione", ex);
            return AuthResult.fail();
        }
    }

    private static boolean isBlank(String s) { return s == null || s.isBlank(); }

    private static boolean passwordMatches(String rawPassword, String dbHashHex) {
        if (isBlank(dbHashHex)) return false;
        String candidateHash = sha256Hex(rawPassword);
        return candidateHash.equalsIgnoreCase(dbHashHex.trim());
    }

    private static String sha256Hex(String rawPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 non disponibile", e);
        }
    }
}