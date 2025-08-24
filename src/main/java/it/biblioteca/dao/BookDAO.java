// src/main/java/it/biblioteca/dao/BookDAO.java
package it.biblioteca.dao;

import it.biblioteca.entity.Book;

import java.util.List;
import java.util.Optional;

public interface BookDAO {
    void salvaLibro(Book book);
    void aggiornaLibro(Book book);
    void eliminaLibro(Long id);           // Cancellazione fisica
    List<Book> trovaTutti();              // Solo libri attivi (se usi colonna attivo)
    Optional<Book> trovaPerId(Long id);
}