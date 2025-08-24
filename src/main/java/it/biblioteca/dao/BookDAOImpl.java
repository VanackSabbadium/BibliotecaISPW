// src/main/java/it/biblioteca/dao/BookDAOImpl.java
package it.biblioteca.dao;

import it.biblioteca.config.DatabaseConfig;
import it.biblioteca.entity.Book;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookDAOImpl implements BookDAO {
    private final DatabaseConfig dbConfig;

    public BookDAOImpl(DatabaseConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    @Override
    public void salvaLibro(Book book) {
        String sql = """
            INSERT INTO libri (isbn, titolo, autore, data_pubblicazione, casa_editrice, attivo, copie)
            VALUES (?, ?, ?, ?, ?, 1, ?)
        """;
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, book.getIsbn());
            ps.setString(2, book.getTitolo());
            ps.setString(3, book.getAutore());
            LocalDate dp = book.getDataPubblicazione();
            if (dp != null) ps.setDate(4, Date.valueOf(dp)); else ps.setNull(4, Types.DATE);
            ps.setString(5, book.getCasaEditrice());
            ps.setInt(6, Math.max(1, book.getCopie()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore nel salvataggio del libro", e);
        }
    }

    @Override
    public void aggiornaLibro(Book book) {
        String sql = """
            UPDATE libri
            SET isbn = ?, titolo = ?, autore = ?, data_pubblicazione = ?, casa_editrice = ?, copie = ?
            WHERE id = ?
        """;
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, book.getIsbn());
            ps.setString(2, book.getTitolo());
            ps.setString(3, book.getAutore());
            LocalDate dp = book.getDataPubblicazione();
            if (dp != null) ps.setDate(4, Date.valueOf(dp)); else ps.setNull(4, Types.DATE);
            ps.setString(5, book.getCasaEditrice());
            ps.setInt(6, Math.max(1, book.getCopie()));
            ps.setLong(7, book.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore nell'aggiornamento del libro", e);
        }
    }

    @Override
    public void eliminaLibro(Long id) {
        String sql = "DELETE FROM libri WHERE id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore nell'eliminazione del libro", e);
        }
    }

    @Override
    public List<Book> trovaTutti() {
        String sql = """
            SELECT id, isbn, titolo, autore, data_pubblicazione, casa_editrice, attivo, copie
            FROM libri
            WHERE attivo = 1
            ORDER BY titolo ASC, id ASC
        """;
        List<Book> books = new ArrayList<>();
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                books.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore nel recupero dei libri attivi", e);
        }
        return books;
    }

    @Override
    public Optional<Book> trovaPerId(Long id) {
        String sql = """
            SELECT id, isbn, titolo, autore, data_pubblicazione, casa_editrice, attivo, copie
            FROM libri
            WHERE id = ?
        """;
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore nella ricerca del libro per id", e);
        }
        return Optional.empty();
    }

    private Book mapRow(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        String isbn = rs.getString("isbn");
        String titolo = rs.getString("titolo");
        String autore = rs.getString("autore");
        Date d = rs.getDate("data_pubblicazione");
        LocalDate dataPubblicazione = d != null ? d.toLocalDate() : null;
        String casaEditrice = rs.getString("casa_editrice");
        boolean attivo = rs.getBoolean("attivo");
        int copie = rs.getInt("copie");
        return new Book(id, isbn, titolo, autore, dataPubblicazione, casaEditrice, attivo, copie);
    }
}