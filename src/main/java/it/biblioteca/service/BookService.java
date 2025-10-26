package it.biblioteca.service;

import it.biblioteca.bean.BookBean;
import it.biblioteca.dao.BookDAO;
import it.biblioteca.entity.Book;
import it.biblioteca.events.EventBus;
import it.biblioteca.events.events.BookChanged;

import java.util.List;

public class BookService {
    private final BookDAO bookDAO;

    public BookService(BookDAO bookDAO) {
        this.bookDAO = bookDAO;
    }

    public List<Book> findAll() {
        return bookDAO.trovaTutti();
    }

    public boolean add(BookBean bean) {
        try {
            Book b = toEntity(bean);
            bookDAO.salvaLibro(b);
            EventBus.getDefault().publish(new BookChanged(BookChanged.Action.ADDED, b.getId()));
            return true;
        } catch (Exception _) {
            return false;
        }
    }

    public boolean update(BookBean bean) {
        try {
            Book b = toEntity(bean);
            bookDAO.aggiornaLibro(b);
            EventBus.getDefault().publish(new BookChanged(BookChanged.Action.UPDATED, b.getId()));
            return true;
        } catch (Exception _) {
            return false;
        }
    }

    public boolean remove(Long id) {
        try {
            bookDAO.eliminaLibro(id);
            EventBus.getDefault().publish(new BookChanged(BookChanged.Action.REMOVED, id));
            return true;
        } catch (Exception _) {
            return false;
        }
    }

    private Book toEntity(BookBean bean) {
        Book b = new Book();
        b.setId(bean.getId());
        b.setIsbn(bean.getIsbn());
        b.setTitolo(bean.getTitolo());
        b.setAutore(bean.getAutore());
        b.setDataPubblicazione(bean.getDataPubblicazione());
        b.setCasaEditrice(bean.getCasaEditrice());
        b.setCopie(bean.getCopie());
        return b;
    }
}