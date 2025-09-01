package it.biblioteca.dao;

import it.biblioteca.db.DatabaseConfig;
import it.biblioteca.security.SessionContext;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnectionProvider implements ConnectionProvider {
    @Override
    public Connection getConnection() throws SQLException {
        Connection conn = DatabaseConfig.getConnection();

        // Se connessi come "Utente", impostiamo la tessera tramite funzione di sessione MySQL
        if (SessionContext.isUtente() && SessionContext.getTessera() != null) {
            try (Statement st = conn.createStatement()) {
                // Imposta il contesto di sessione per la vista 'utenti_self'
                st.execute("SELECT biblioteca.set_app_tessera(" + SessionContext.getTessera() + ")");
            }
        }

        return conn;
    }
}