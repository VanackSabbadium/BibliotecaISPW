package it.biblioteca.dao.db;

import it.biblioteca.dao.BookDAO;
import it.biblioteca.entity.Book;
import it.biblioteca.testutil.TestConnectionProvider;
import it.biblioteca.testutil.TestDbSetup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BookDaoDbTest {

    private TestConnectionProvider cp;
    private BookDAO dao;

    @BeforeEach
    void setup() throws Exception {
        cp = new TestConnectionProvider();
        try (Connection c = cp.getConnection()) {
            TestDbSetup.resetSchema(c);
        }
        dao = new DbBookDAO(cp);
    }

    @Test
    void crudLibro_funzionante() {
        // Crea
        Book b = new Book();
        b.setIsbn("978000000002");
        b.setTitolo("Nuovo Libro");
        b.setAutore("Autore X");
        b.setCasaEditrice("Editore X");
        b.setDataPubblicazione(LocalDate.of(2022, 5, 20));
        b.setCopie(2);

        dao.salvaLibro(b);
        assertNotNull(b.getId());

        List<Book> all = dao.trovaTutti();
        assertEquals(2, all.size(), "C'è anche il libro seed inserito in setup");
        Book found = all.stream().filter(x -> "978000000002".equals(x.getIsbn())).findFirst().orElseThrow();
        assertEquals("Nuovo Libro", found.getTitolo());

        found.setTitolo("Nuovo Libro 2");
        found.setCopie(5);
        dao.aggiornaLibro(found);

        Book after = dao.trovaTutti().stream().filter(x -> "978000000002".equals(x.getIsbn())).findFirst().orElseThrow();
        assertEquals("Nuovo Libro 2", after.getTitolo());
        assertEquals(5, after.getCopie());

        dao.eliminaLibro(after.getId());
        boolean stillThere = dao.trovaTutti().stream().anyMatch(x -> "978000000002".equals(x.getIsbn()));
        assertFalse(stillThere);
    }
}
