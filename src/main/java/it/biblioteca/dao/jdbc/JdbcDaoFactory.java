package it.biblioteca.dao.jdbc;

import it.biblioteca.dao.BookDAO;
import it.biblioteca.dao.ConnectionProvider;
import it.biblioteca.dao.DaoFactory;
import it.biblioteca.dao.PrestitoDAO;
import it.biblioteca.dao.UtenteDAO;

public class JdbcDaoFactory implements DaoFactory {
    private final ConnectionProvider cp;
    private BookDAO bookDAO;
    private UtenteDAO utenteDAO;
    private PrestitoDAO prestitoDAO;

    public JdbcDaoFactory(ConnectionProvider cp) {
        this.cp = cp;
    }

    @Override
    public BookDAO bookDAO() {
        if (bookDAO == null) bookDAO = new JdbcBookDAO(cp);
        return bookDAO;
    }

    @Override
    public UtenteDAO utenteDAO() {
        if (utenteDAO == null) utenteDAO = new JdbcUtenteDAO(cp);
        return utenteDAO;
    }

    @Override
    public PrestitoDAO prestitoDAO() {
        if (prestitoDAO == null) prestitoDAO = new JdbcPrestitoDAO(cp);
        return prestitoDAO;
    }
}