package it.biblioteca.dao.jdbc;

import it.biblioteca.bean.PrenotazioneBean;
import it.biblioteca.dao.PrenotazioneDAO;
import it.biblioteca.db.DatabaseConfig;
import it.biblioteca.entity.Prenotazione;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class JdbcPrenotazioneDAO implements PrenotazioneDAO {

    public JdbcPrenotazioneDAO() {}

    @Override
    public List<Prenotazione> trovaTutte() {
        String sql = "SELECT id, libro_id, utente_id, data_prenotazione, data_evasione, libro_titolo_snapshot, utente_snapshot FROM prenotazioni ORDER BY data_prenotazione DESC";
        List<Prenotazione> out = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Prenotazione p = mapRow(rs);
                out.add(p);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore trovaTutte prenotazioni", e);
        }
        return out;
    }

    @Override
    public List<Prenotazione> trovaPrenotazioniAttive() {
        String sql = "SELECT id, libro_id, utente_id, data_prenotazione, data_evasione, libro_titolo_snapshot, utente_snapshot FROM prenotazioni WHERE data_evasione IS NULL ORDER BY data_prenotazione ASC";
        List<Prenotazione> out = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Errore trovaPrenotazioniAttive", e);
        }
        return out;
    }

    @Override
    public boolean inserisci(PrenotazioneBean bean) {
        if (bean == null || bean.getLibroId() == null || bean.getUtenteId() == null) return false;
        LocalDate dp = bean.getDataPrenotazione() != null ? bean.getDataPrenotazione() : LocalDate.now();

        String sql = "INSERT INTO prenotazioni (libro_id, utente_id, data_prenotazione, libro_titolo_snapshot, utente_snapshot) VALUES (?,?,?,?,?)";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setObject(1, bean.getLibroId(), Types.BIGINT);
            ps.setObject(2, bean.getUtenteId(), Types.BIGINT);
            ps.setDate(3, Date.valueOf(dp));
            ps.setString(4, bean.getLibroTitoloSnapshot());
            ps.setString(5, bean.getUtenteSnapshot());

            int n = ps.executeUpdate();
            return n > 0;
        } catch (SQLException e) {
            System.err.println("JdbcPrenotazioneDAO.inserisci() fallita: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean evadiPrenotazione(Long prenotazioneId, LocalDate dataEvasione) {
        if (prenotazioneId == null) return false;
        String sql = "UPDATE prenotazioni SET data_evasione = ? WHERE id = ? AND data_evasione IS NULL";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(dataEvasione != null ? dataEvasione : LocalDate.now()));
            ps.setLong(2, prenotazioneId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("JdbcPrenotazioneDAO.evadiPrenotazione() errore: " + e.getMessage());
            return false;
        }
    }

    @Override
    public int contaPrenotazioniAttivePerLibro(Long libroId) {
        if (libroId == null) return 0;
        String sql = "SELECT COUNT(*) AS cnt FROM prenotazioni WHERE libro_id = ? AND data_evasione IS NULL";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, libroId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("cnt");
            }
        } catch (SQLException e) {
            System.err.println("JdbcPrenotazioneDAO.contaPrenotazioniAttivePerLibro() errore: " + e.getMessage());
        }
        return 0;
    }

    private Prenotazione mapRow(ResultSet rs) throws SQLException {
        Prenotazione p = new Prenotazione();
        p.setId(rs.getObject("id") != null ? rs.getLong("id") : null);
        Object lib = rs.getObject("libro_id");
        p.setLibroId(lib != null ? ((Number) lib).longValue() : null);
        Object ut = rs.getObject("utente_id");
        p.setUtenteId(ut != null ? ((Number) ut).longValue() : null);

        Date dp = rs.getDate("data_prenotazione");
        p.setDataPrenotazione(dp != null ? dp.toLocalDate() : null);

        Date de = rs.getDate("data_evasione");
        p.setDataEvasione(de != null ? de.toLocalDate() : null);

        p.setLibroTitoloSnapshot(rs.getString("libro_titolo_snapshot"));
        p.setUtenteSnapshot(rs.getString("utente_snapshot"));
        return p;
    }
}
