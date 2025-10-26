package it.biblioteca.dao.json;

import it.biblioteca.dao.BookDAO;
import it.biblioteca.dao.DaoFactory;
import it.biblioteca.dao.PrestitoDAO;
import it.biblioteca.dao.UtenteDAO;

import java.io.File;

public class JsonDaoFactory implements DaoFactory {

    private final BookDAO bookDAO;
    private final UtenteDAO utenteDAO;
    private final PrestitoDAO prestitoDAO;

    public JsonDaoFactory(File baseDir) {
        this.bookDAO = new JsonBookDAO(baseDir);
        this.utenteDAO = new JsonUtenteDAO(baseDir);
        this.prestitoDAO = new JsonPrestitoDAO(baseDir);
    }

    @Override
    public BookDAO bookDAO() {
        return bookDAO;
    }

    @Override
    public UtenteDAO utenteDAO() {
        return utenteDAO;
    }

    @Override
    public PrestitoDAO prestitoDAO() {
        return prestitoDAO;
    }
}