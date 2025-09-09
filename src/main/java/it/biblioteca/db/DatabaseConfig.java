package it.biblioteca.db;

import it.biblioteca.ui.StartupResult;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Config centrale per le connessioni DB.
 *
 * - Il progetto usa un "service DB user" per le query interne (es. Admin/admin).
 * - Quando un utente si autentica, l'app NON deve usare le sue credenziali per aprire
 *   le connessioni JDBC dei DAO: i DAO devono usare l'account di servizio.
 *
 * Questo DatabaseConfig mantiene:
 * - username/password "correnti" usate dai DAO (impostate con apply(...) o applyServiceCredentials()).
 * - costanti SERVICE_DB_USER/ PASS usate da AuthService per autenticare gli user app.
 */
public final class DatabaseConfig {
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "3306";
    private static final String DEFAULT_DATABASE = "biblioteca";

    // ACCOUNT DI SERVIZIO (usato per leggere la tabella credenziali e per le operazioni DAO)
    // Assicurati che questo account esista nel DB e abbia i privilegi necessari (lo script che mi hai fornito
    // crea l'utente 'Admin' con password 'admin').
    private static final String SERVICE_DB_USER = "Admin";
    private static final String SERVICE_DB_PASS = "admin";

    // Credenziali che i DAO useranno: possono essere impostate tramite apply(StartupResult) oppure
    // applyServiceCredentials() per tornare all'account di servizio.
    private static volatile String username;
    private static volatile String password;

    private DatabaseConfig() {}

    public static String getHost() { return DEFAULT_HOST; }
    public static String getPort() { return DEFAULT_PORT; }
    public static String getDatabase() { return DEFAULT_DATABASE; }

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

    /**
     * Imposta esplicitamente le credenziali di servizio (Admin/admin di default).
     * Chiamare dopo che l'utente applicativo è stato autenticato correttamente.
     */
    public static void applyServiceCredentials() {
        username = SERVICE_DB_USER;
        password = SERVICE_DB_PASS;
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

    /**
     * Connessione diretta usando l'account di servizio (indipendente dallo stato 'username/password' corrente).
     * Utile per AuthService (verificare le credenziali nella tabella app-level).
     */
    public static Connection getServiceConnection() throws SQLException {
        return DriverManager.getConnection(buildJdbcUrl(), SERVICE_DB_USER, SERVICE_DB_PASS);
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

    public static String getServiceDbUser() { return SERVICE_DB_USER; }
    public static String getServiceDbPassword() { return SERVICE_DB_PASS; }
}
