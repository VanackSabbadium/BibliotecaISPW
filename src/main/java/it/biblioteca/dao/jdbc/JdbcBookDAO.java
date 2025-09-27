package it.biblioteca.dao.jdbc;

import it.biblioteca.dao.BookDAO;
import it.biblioteca.dao.ConnectionProvider;
import it.biblioteca.dao.DatabaseConnectionProvider;
import it.biblioteca.entity.Book;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcBookDAO implements BookDAO {
    private final ConnectionProvider cp;

    public JdbcBookDAO() {
        this(new DatabaseConnectionProvider());
    }

    public JdbcBookDAO(ConnectionProvider cp) {
        this.cp = cp;
    }

    @Override
    public void salvaLibro(Book b) {
        String sql = "INSERT INTO libri (isbn, titolo, autore, casa_editrice, data_pubblicazione, copie) VALUES (?,?,?,?,?,?)";
        try (Connection c = cp.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, b.getIsbn());
            ps.setString(2, b.getTitolo());
            ps.setString(3, b.getAutore());
            ps.setString(4, b.getCasaEditrice());
            if (b.getDataPubblicazione() != null) {
                ps.setDate(5, Date.valueOf(b.getDataPubblicazione()));
            } else {
                ps.setNull(5, Types.DATE);
            }
            ps.setInt(6, b.getCopie());

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) b.setId(keys.getLong(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore salvaLibro", e);
        }
    }

    @Override
    public void aggiornaLibro(Book b) {
        String sql = "UPDATE libri SET isbn=?, titolo=?, autore=?, casa_editrice=?, data_pubblicazione=?, copie=? WHERE id=?";
        try (Connection c = cp.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, b.getIsbn());
            ps.setString(2, b.getTitolo());
            ps.setString(3, b.getAutore());
            ps.setString(4, b.getCasaEditrice());
            if (b.getDataPubblicazione() != null) {
                ps.setDate(5, Date.valueOf(b.getDataPubblicazione()));
            } else {
                ps.setNull(5, Types.DATE);
            }
            ps.setInt(6, b.getCopie());
            ps.setLong(7, b.getId());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore aggiornaLibro", e);
        }
    }

    @Override
    public void eliminaLibro(Long id) {
        String sql = "DELETE FROM libri WHERE id=?";
        try (Connection c = cp.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore eliminaLibro", e);
        }
    }

    @Override
    public List<Book> trovaTutti() {
        String sql = "SELECT id, isbn, titolo, autore, casa_editrice, data_pubblicazione, copie FROM libri WHERE attivo = 1";
        List<Book> out = new ArrayList<>();
        try (Connection c = cp.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Book b = new Book();
                b.setId(rs.getLong("id"));
                b.setIsbn(rs.getString("isbn"));
                b.setTitolo(rs.getString("titolo"));
                b.setAutore(rs.getString("autore"));
                b.setCasaEditrice(rs.getString("casa_editrice"));
                Date d = rs.getDate("data_pubblicazione");
                b.setDataPubblicazione(d != null ? d.toLocalDate() : null);
                b.setCopie(rs.getInt("copie"));
                out.add(b);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore trovaTutti libri", e);
        }
        return out;
    }

}