// src/main/java/it/biblioteca/controller/BookController.java
package it.biblioteca.controller;

import it.biblioteca.bean.BookBean;
import it.biblioteca.dao.BookDAO;
import it.biblioteca.entity.Book;

import java.util.List;
// import java.util.Optional;

public class BookController {
    private final BookDAO dao;
    private final PrestitoController prestitoController;

    public BookController(BookDAO dao, PrestitoController prestitoController) {
        this.dao = dao;
        this.prestitoController = prestitoController;
    }

    public boolean aggiungiLibro(BookBean bean) {
        if (!validaLibro(bean)) return false;
        Book b = toEntity(bean);
        try {
            dao.salvaLibro(b);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean aggiornaLibro(BookBean bean) {
        if (bean == null || bean.getId() == null || !validaLibro(bean)) return false;
        Book b = toEntity(bean);
        b.setId(bean.getId());
        try {
            dao.aggiornaLibro(b);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean rimuoviLibro(Long id) {
        if (id == null) return false;
        if (prestitoController.esistonoPrestitiAttiviPerLibro(id)) {
            return false; // non eliminare se ci sono prestiti attivi
        }
        try {
            dao.eliminaLibro(id); // eliminazione fisica
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public List<Book> trovaTutti() {
        return dao.trovaTutti();
    }

    // public Optional<Book> trovaPerId(Long id) { return dao.trovaPerId(id); }

    private boolean validaLibro(BookBean bean) {
        return bean != null &&
                bean.getIsbn() != null && !bean.getIsbn().isBlank() &&
                bean.getTitolo() != null && !bean.getTitolo().isBlank() &&
                bean.getAutore() != null && !bean.getAutore().isBlank() &&
                bean.getDataPubblicazione() != null &&
                bean.getCasaEditrice() != null && !bean.getCasaEditrice().isBlank() &&
                bean.getCopie() >= 1;
    }

    private Book toEntity(BookBean bean) {
        Book b = new Book();
        b.setIsbn(bean.getIsbn());
        b.setTitolo(bean.getTitolo());
        b.setAutore(bean.getAutore());
        b.setDataPubblicazione(bean.getDataPubblicazione());
        b.setCasaEditrice(bean.getCasaEditrice());
        b.setCopie(bean.getCopie());
        b.setAttivo(true);
        return b;
    }
}