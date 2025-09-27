package it.biblioteca.db;

import it.biblioteca.ui.StartupResult;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConfig {
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "3306";
    private static final String DEFAULT_DATABASE = "biblioteca";

    private static volatile String username;
    private static volatile String password;

    private DatabaseConfig() {}

    public static String buildJdbcUrl() {
        return "jdbc:mariadb://" + DEFAULT_HOST + ":" + DEFAULT_PORT + "/" + DEFAULT_DATABASE;
    }

    /**
     * Imposta le credenziali "correnti" (utili se vuoi che i DAO usino user/pass specifici).
     * Nelle nostre scelte, non useremo le credenziali dell'utente applicazione per i DAO:
     * dopo il login dell'utente applicativo dobbiamo chiamare applyServiceCredentials()
     * per far usare ai DAO l'account di servizio.
     */
    public static void apply(StartupResult res) {
        if (res == null) return;
        username = res.getUsername();
        password = res.getPassword();
    }

    public static boolean isConfigured() {
        return username != null && !username.isBlank() &&
                password != null && !password.isBlank();
    }

    public static Connection getConnection() throws SQLException {
        if (!isConfigured()) {
            throw new SQLException("Credenziali DB non configurate per i DAO. Chiama DatabaseConfig.applyServiceCredentials() prima.");
        }
        return DriverManager.getConnection(buildJdbcUrl(), username, password);
    }

    // Metodi di utilità per testare credenziali: lascio così per compatibilità, ma non usiamo più
    // testCredentials(user,pass) per l'autenticazione applicativa.
    public static boolean testCredentials(String user, String pass) {
        try (Connection ignored = DriverManager.getConnection(buildJdbcUrl(), user, pass)) {
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
}
