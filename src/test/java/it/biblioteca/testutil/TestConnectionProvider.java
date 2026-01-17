package it.biblioteca.testutil;

import it.biblioteca.dao.ConnectionProvider;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestConnectionProvider implements ConnectionProvider {

    public static final String DEFAULT_URL  =
            System.getProperty("DB_URL", "jdbc:mysql://localhost:3306/biblioteca?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
    public static final String DEFAULT_USER =
            System.getProperty("DB_USER", "Admin");
    public static final String DEFAULT_PASS =
            System.getProperty("DB_PASS", "admin");

    private final String url;
    private final String user;
    private final String pass;

    public TestConnectionProvider() {
        this(DEFAULT_URL, DEFAULT_USER, DEFAULT_PASS);
    }

    public TestConnectionProvider(String url, String user, String pass) {
        this.url = url;
        this.user = user;
        this.pass = pass;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, pass);
    }
}
