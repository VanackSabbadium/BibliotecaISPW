package it.biblioteca.service;

import it.biblioteca.dao.DaoFactory;
import it.biblioteca.dao.ConnectionProvider;
import it.biblioteca.dao.jdbc.JdbcDaoFactory;

public class AppContext {
    private final DaoFactory daoFactory;
    private final BookService bookService;
    private final UtenteService utenteService;
    private final PrestitoService prestitoService;

    public AppContext(ConnectionProvider cp) {
        this(new JdbcDaoFactory(cp));
    }

    public AppContext(DaoFactory daoFactory) {
        this.daoFactory = daoFactory;
        this.bookService = new BookService(daoFactory.bookDAO());
        this.utenteService = new UtenteService(daoFactory.utenteDAO());
        this.prestitoService = new PrestitoService(daoFactory.prestitoDAO(), daoFactory.bookDAO(), daoFactory.utenteDAO());
    }

    public DaoFactory dao() { return daoFactory; }
    public BookService books() { return bookService; }
    public UtenteService users() { return utenteService; }
    public PrestitoService loans() { return prestitoService; }
}