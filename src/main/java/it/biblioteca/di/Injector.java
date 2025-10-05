package it.biblioteca.di;

import it.biblioteca.controller.BookController;
import it.biblioteca.controller.PrestitoController;
import it.biblioteca.controller.UtenteController;
import it.biblioteca.dao.ConnectionProvider;
import it.biblioteca.dao.jdbc.JdbcDaoFactory;
import it.biblioteca.service.AppContext;

public class Injector {
    private final AppContext ctx;
    private BookController bookController;
    private PrestitoController prestitoController;
    private UtenteController utenteController;

    public Injector(ConnectionProvider cp) {
        this.ctx = new AppContext(new JdbcDaoFactory(cp));
    }

    public BookController bookController() {
        if (bookController == null) bookController = new BookController(ctx.books());
        return bookController;
    }

    public PrestitoController prestitoController() {
        if (prestitoController == null) prestitoController = new PrestitoController(ctx.loans());
        return prestitoController;
    }

    public UtenteController utenteController() {
        if (utenteController == null) utenteController = new UtenteController(ctx.users());
        return utenteController;
    }

    public AppContext context() {
        return ctx;
    }
}