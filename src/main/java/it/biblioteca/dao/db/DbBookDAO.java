package it.biblioteca.dao.db;

import it.biblioteca.dao.BookDAO;
import it.biblioteca.dao.ConnectionProvider;
import it.biblioteca.entity.Book;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DbBookDAO implements BookDAO {

    private final ConnectionProvider cp;

    public DbBookDAO(ConnectionProvider cp) {
        this.cp = cp;
    }

    @Override
    public void salvaLibro(Book book) {
        final String sql = """
                INSERT INTO libri(isbn,titolo,autore,data_pubblicazione,casa_editrice,attivo,copie)
                VALUES (?,?,?,?,?,?,?)
                """;

        try (Connection conn = cp.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, book.getIsbn());
            ps.setString(2, book.getTitolo());
            ps.setString(3, book.getAutore());

            if (book.getDataPubblicazione() != null) {
                ps.setDate(4, Date.valueOf(book.getDataPubblicazione()));
            } else {
                ps.setDate(4, null);
            }

            ps.setString(5, book.getCasaEditrice());

            ps.setBoolean(6, true);

            ps.setInt(7, book.getCopie());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long newId = rs.getLong(1);
                    book.setId(newId);
                }
            }
        } catch (SQLException e) {
            throw new IllegalArgumentException("Errore salvaLibro", e);
        }
    }

    @Override
    public void aggiornaLibro(Book book) {
        final String sql = """
                UPDATE libri
                SET isbn=?, titolo=?, autore=?, data_pubblicazione=?, casa_editrice=?, attivo=?, copie=?
                WHERE id=?
                """;

        try (Connection conn = cp.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, book.getIsbn());
            ps.setString(2, book.getTitolo());
            ps.setString(3, book.getAutore());

            if (book.getDataPubblicazione() != null) {
                ps.setDate(4, Date.valueOf(book.getDataPubblicazione()));
            } else {
                ps.setDate(4, null);
            }

            ps.setString(5, book.getCasaEditrice());

            ps.setBoolean(6, true);

            ps.setInt(7, book.getCopie());

            ps.setLong(8, book.getId() != null ? book.getId() : -1L);

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalArgumentException("Errore aggiornaLibro", e);
        }
    }

    @Override
    public void eliminaLibro(Long id) {
        final String sql = "DELETE FROM libri WHERE id=?";
        try (Connection conn = cp.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id != null ? id : -1L);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalArgumentException("Errore eliminaLibro", e);
        }
    }

    @Override
    public List<Book> trovaTutti() {
        final String sql = """
                SELECT id,isbn,titolo,autore,data_pubblicazione,casa_editrice,attivo,copie
                FROM libri
                ORDER BY titolo
                """;
        List<Book> out = new ArrayList<>();

        try (Connection conn = cp.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                out.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new IllegalArgumentException("Errore trovaTutti libri", e);
        }

        return out;
    }

    private Book mapRow(ResultSet rs) throws SQLException {
        Book b = new Book();

        Object idObj = rs.getObject("id");
        if (idObj instanceof Number n) {
            b.setId(n.longValue());
        }

        b.setIsbn(rs.getString("isbn"));
        b.setTitolo(rs.getString("titolo"));
        b.setAutore(rs.getString("autore"));

        Date d = rs.getDate("data_pubblicazione");
        b.setDataPubblicazione(d != null ? d.toLocalDate() : null);

        b.setCasaEditrice(rs.getString("casa_editrice"));

        boolean attivoDb = rs.getBoolean("attivo");
        b.setAttivo(attivoDb);

        int copieDb = rs.getInt("copie");
        b.setCopie(copieDb);

        return b;
    }
}