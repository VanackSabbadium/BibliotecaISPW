package it.biblioteca.dao.db;

import it.biblioteca.dao.BookDAO;
import it.biblioteca.dao.ConnectionProvider;
import it.biblioteca.dao.DaoFactory;
import it.biblioteca.dao.PrestitoDAO;
import it.biblioteca.dao.UtenteDAO;

public class DbDaoFactory implements DaoFactory {

    private final BookDAO bookDAO;
    private final UtenteDAO utenteDAO;
    private final PrestitoDAO prestitoDAO;

    public DbDaoFactory(ConnectionProvider cp) {
        this.bookDAO = new DbBookDAO(cp);
        this.utenteDAO = new DbUtenteDAO(cp);
        this.prestitoDAO = new DbPrestitoDAO(cp);
    }

    @Override
    public BookDAO bookDAO() { return bookDAO; }

    @Override
    public UtenteDAO utenteDAO() { return utenteDAO; }

    @Override
    public PrestitoDAO prestitoDAO() { return prestitoDAO; }
}