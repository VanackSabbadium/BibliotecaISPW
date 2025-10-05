package it.biblioteca.dao.jdbc;

import it.biblioteca.dao.BookDAO;
import it.biblioteca.dao.ConnectionProvider;
import it.biblioteca.entity.Book;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class JdbcBookDAO extends JdbcSupport implements BookDAO {
    public JdbcBookDAO(ConnectionProvider cp) {
        super(cp);
    }

    @Override
    public void salvaLibro(Book book) {
        String sql = "INSERT INTO libri(isbn,titolo,autore,data_pubblicazione,casa_editrice,attivo,copie) VALUES (?,?,?,?,?,?,?)";
        try {
            long id = insertAndReturnKey(sql, ps -> {
                ps.setString(1, book.getIsbn());
                ps.setString(2, book.getTitolo());
                ps.setString(3, book.getAutore());
                LocalDate d = book.getDataPubblicazione();
                ps.setDate(4, d != null ? Date.valueOf(d) : null);
                ps.setString(5, book.getCasaEditrice());
                ps.setBoolean(6, true);
                ps.setInt(7, book.getCopie());
            });
            if (id > 0) book.setId(id);
        } catch (SQLException e) {
            throw new RuntimeException("Errore salvaLibro", e);
        }
    }

    @Override
    public void aggiornaLibro(Book book) {
        String sql = "UPDATE libri SET isbn=?,titolo=?,autore=?,data_pubblicazione=?,casa_editrice=?,copie=? WHERE id=?";
        try {
            update(sql, ps -> {
                ps.setString(1, book.getIsbn());
                ps.setString(2, book.getTitolo());
                ps.setString(3, book.getAutore());
                LocalDate d = book.getDataPubblicazione();
                ps.setDate(4, d != null ? Date.valueOf(d) : null);
                ps.setString(5, book.getCasaEditrice());
                ps.setInt(6, book.getCopie());
                ps.setLong(7, book.getId());
            });
        } catch (SQLException e) {
            throw new RuntimeException("Errore aggiornaLibro", e);
        }
    }

    @Override
    public void eliminaLibro(Long id) {
        String sql = "DELETE FROM libri WHERE id=?";
        try {
            update(sql, ps -> ps.setLong(1, id));
        } catch (SQLException e) {
            throw new RuntimeException("Errore eliminaLibro", e);
        }
    }

    @Override
    public List<Book> trovaTutti() {
        String sql = "SELECT id,isbn,titolo,autore,data_pubblicazione,casa_editrice,copie FROM libri ORDER BY titolo";
        try {
            return query(sql, this::map);
        } catch (SQLException e) {
            throw new RuntimeException("Errore trovaTutti libri", e);
        }
    }

    private Book map(ResultSet rs) throws SQLException {
        Book b = new Book();
        b.setId(rs.getLong("id"));
        b.setIsbn(rs.getString("isbn"));
        b.setTitolo(rs.getString("titolo"));
        b.setAutore(rs.getString("autore"));
        Date d = rs.getDate("data_pubblicazione");
        b.setDataPubblicazione(d != null ? d.toLocalDate() : null);
        b.setCasaEditrice(rs.getString("casa_editrice"));
        b.setCopie(rs.getInt("copie"));
        return b;
    }
}