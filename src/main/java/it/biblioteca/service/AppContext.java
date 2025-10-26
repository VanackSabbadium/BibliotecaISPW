package it.biblioteca.service;

import it.biblioteca.dao.BookDAO;
import it.biblioteca.dao.DaoFactory;
import it.biblioteca.dao.PrestitoDAO;
import it.biblioteca.dao.UtenteDAO;

public class AppContext {

    private final BookService bookService;
    private final PrestitoService prestitoService;
    private final UtenteService utenteService;

    public AppContext(DaoFactory daoFactory) {
        if (daoFactory == null) {
            throw new IllegalArgumentException("DaoFactory non può essere null");
        }

        BookDAO bookDAO = daoFactory.bookDAO();
        PrestitoDAO prestitoDAO = daoFactory.prestitoDAO();
        UtenteDAO utenteDAO = daoFactory.utenteDAO();

        this.bookService = new BookService(bookDAO);
        this.prestitoService = new PrestitoService(prestitoDAO, bookDAO, utenteDAO);
        this.utenteService = new UtenteService(utenteDAO);
    }

    public BookService books() {
        return bookService;
    }

    public PrestitoService loans() {
        return prestitoService;
    }

    public UtenteService users() {
        return utenteService;
    }
}