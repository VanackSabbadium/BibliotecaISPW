package it.biblioteca.dao;

import it.biblioteca.db.DatabaseConfig;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnectionProvider implements ConnectionProvider {
    @Override
    public Connection getConnection() throws SQLException {
        Connection conn = DatabaseConfig.getConnection();

        return conn;
    }
}