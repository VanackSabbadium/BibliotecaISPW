// src/main/java/it/biblioteca/config/DatabaseConfig.java
package it.biblioteca.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {
    private final String dbUrl = "jdbc:mariadb://localhost:3306/biblioteca";
    private final String username;
    private final String password;

    public DatabaseConfig(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, username, password);
    }

    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn.isValid(5);
        } catch (SQLException e) {
            throw new RuntimeException("Errore di connessione al database", e);
        }
    }
}
