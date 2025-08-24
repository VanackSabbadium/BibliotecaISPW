package it.biblioteca.db;

import it.biblioteca.ui.StartupResult;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConfig {
    // Impostazioni implicite e nascoste all'utente
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "3306";
    private static final String DEFAULT_DATABASE = "biblioteca";

    // Credenziali impostate all'avvio dall'utente
    private static volatile String username;
    private static volatile String password;

    private DatabaseConfig() {}

    public static void apply(StartupResult res) {
        if (res == null || !res.isValid()) return;
        username = res.getUsername();
        password = res.getPassword();
    }

    public static boolean isConfigured() {
        return username != null && !username.isBlank() &&
                password != null && !password.isBlank();
    }

    public static String getUsername() { return username; }
    public static String getPassword() { return password; }

    // Parametri impliciti
    public static String getHost() { return DEFAULT_HOST; }
    public static String getPort() { return DEFAULT_PORT; }
    public static String getDatabase() { return DEFAULT_DATABASE; }

    public static String buildJdbcUrl() {
        // Adatta al tuo driver se differente (es. MySQL, SQLServer)
        return "jdbc:mariadb://" + DEFAULT_HOST + ":" + DEFAULT_PORT + "/" + DEFAULT_DATABASE;
    }

    // Verifica le credenziali provando ad aprire una connessione
    public static boolean testCredentials(String user, String pass) {
        try (Connection ignored = DriverManager.getConnection(buildJdbcUrl(), user, pass)) {
            return true;
        } catch (SQLException e) {
            // Login fallito/credenziali non valide o altra eccezione JDBC
            return false;
        }
    }

    // Comodo per i DAO se vuoi centralizzare
    public static Connection getConnection() throws SQLException {
        if (!isConfigured()) {
            throw new SQLException("Credenziali non configurate.");
        }
        return DriverManager.getConnection(buildJdbcUrl(), username, password);
    }
}