package it.biblioteca.dao.jdbc;

import it.biblioteca.bean.PrestitoBean;
import it.biblioteca.dao.ConnectionProvider;
import it.biblioteca.dao.DatabaseConnectionProvider;
import it.biblioteca.dao.PrestitoDAO;
import it.biblioteca.entity.Prestito;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class JdbcPrestitoDAO implements PrestitoDAO {
    private final ConnectionProvider cp;

    public JdbcPrestitoDAO() { this(new DatabaseConnectionProvider()); }
    public JdbcPrestitoDAO(ConnectionProvider cp) { this.cp = cp; }

    @Override
    public List<Prestito> trovaTutti() {
        String sql = "SELECT id, libro_id, utente, data_prestito, data_restituzione, libro_titolo_snapshot FROM prestiti";
        List<Prestito> out = new ArrayList<>();
        try (Connection c = cp.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Prestito p = mapRow(rs);
                out.add(p);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore trovaTutti prestiti", e);
        }
        return out;
    }

    @Override
    public List<Prestito> trovaPrestitiAttivi() {
        String sql = "SELECT id, libro_id, utente, data_prestito, data_restituzione, libro_titolo_snapshot FROM prestiti WHERE data_restituzione IS NULL";
        List<Prestito> out = new ArrayList<>();
        try (Connection c = cp.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Prestito p = mapRow(rs);
                out.add(p);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore trovaPrestitiAttivi", e);
        }
        return out;
    }

    @Override
    public boolean inserisci(PrestitoBean bean) {
        // Adatta i getter se i nomi nel tuo PrestitoBean differiscono
        String sql = "INSERT INTO prestiti (libro_id, utente, data_prestito, libro_titolo_snapshot) VALUES (?,?,?,?)";
        try (Connection c = cp.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // getLibroId(), getUtente(), getDataPrestito(), getLibroTitoloSnapshot() sono ipotizzati
            Long libroId = getLibroId(bean);
            String utente = getUtente(bean);
            LocalDate dataPrestito = getDataPrestito(bean);
            String titoloSnap = getLibroTitoloSnapshot(bean);

            if (libroId != null) ps.setLong(1, libroId); else ps.setNull(1, Types.BIGINT);
            ps.setString(2, utente);
            ps.setDate(3, Date.valueOf(dataPrestito != null ? dataPrestito : LocalDate.now()));
            ps.setString(4, titoloSnap);

            int n = ps.executeUpdate();
            return n > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public boolean chiudiPrestito(Long prestitoId, LocalDate dataRestituzione) {
        String sql = "UPDATE prestiti SET data_restituzione=? WHERE id=? AND data_restituzione IS NULL";
        try (Connection c = cp.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(dataRestituzione != null ? dataRestituzione : LocalDate.now()));
            ps.setLong(2, prestitoId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    private Prestito mapRow(ResultSet rs) throws SQLException {
        Prestito p = new Prestito();
        p.setId(rs.getLong("id"));
        Object libroId = rs.getObject("libro_id");
        p.setLibroId(libroId != null ? ((Number) libroId).longValue() : null);
        p.setUtente(rs.getString("utente"));
        Date dp = rs.getDate("data_prestito");
        p.setDataPrestito(dp != null ? dp.toLocalDate() : null);
        Date dr = rs.getDate("data_restituzione");
        p.setDataRestituzione(dr != null ? dr.toLocalDate() : null);
        p.setLibroTitoloSnapshot(rs.getString("libro_titolo_snapshot"));
        return p;
    }

    // Helper per ottenere valori dal PrestitoBean senza conoscere i nomi esatti dei metodi
    private Long getLibroId(PrestitoBean bean) {
        try {
            return (Long) bean.getClass().getMethod("getLibroId").invoke(bean);
        } catch (Exception ignore) { return null; }
    }
    private String getUtente(PrestitoBean bean) {
        try {
            Object v = bean.getClass().getMethod("getUtente").invoke(bean);
            return v != null ? v.toString() : null;
        } catch (Exception ignore) { return null; }
    }
    private LocalDate getDataPrestito(PrestitoBean bean) {
        try {
            return (LocalDate) bean.getClass().getMethod("getDataPrestito").invoke(bean);
        } catch (Exception ignore) { return null; }
    }
    private String getLibroTitoloSnapshot(PrestitoBean bean) {
        try {
            Object v = bean.getClass().getMethod("getLibroTitoloSnapshot").invoke(bean);
            return v != null ? v.toString() : null;
        } catch (Exception ignore) { return null; }
    }
}
