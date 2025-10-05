package it.biblioteca.controller;

import it.biblioteca.bean.BookBean;
import it.biblioteca.entity.Book;
import it.biblioteca.service.BookService;

import java.util.List;

public class BookController {
    private final BookService service;

    public BookController(BookService service) {
        this.service = service;
    }

    public List<Book> trovaTutti() { return service.findAll(); }
    public boolean aggiungiLibro(BookBean bean) { return service.add(bean); }
    public boolean aggiornaLibro(BookBean bean) { return service.update(bean); }
    public boolean rimuoviLibro(Long id) { return service.remove(id); }
}
