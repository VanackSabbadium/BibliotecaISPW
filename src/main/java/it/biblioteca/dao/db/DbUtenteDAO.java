package it.biblioteca.dao.db;

import it.biblioteca.dao.ConnectionProvider;
import it.biblioteca.dao.jdbc.JdbcUtenteDAO;

public class DbUtenteDAO extends JdbcUtenteDAO {

    public DbUtenteDAO(ConnectionProvider cp) {
        super(cp);
    }
}