package it.biblioteca.dao;

import it.biblioteca.entity.Book;

import java.util.List;

public interface BookDAO {

    void salvaLibro(Book book);
    void aggiornaLibro(Book book);
    void eliminaLibro(Long id);
    List<Book> trovaTutti();
}