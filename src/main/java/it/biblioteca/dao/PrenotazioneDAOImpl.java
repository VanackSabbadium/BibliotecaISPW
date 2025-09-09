package it.biblioteca.dao;

import it.biblioteca.bean.PrenotazioneBean;
import it.biblioteca.db.DatabaseConfig;
import it.biblioteca.entity.Prenotazione;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PrenotazioneDAOImpl implements PrenotazioneDAO {

    @Override
    public List<Prenotazione> trovaTutte() {
        String sql = "SELECT id, libro_id, utente_id, data_prenotazione, data_evasione, libro_titolo_snapshot, utente_snapshot FROM prenotazioni ORDER BY id DESC";
        List<Prenotazione> out = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(mapRow(rs));
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
            while (rs.next()) {
                out.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore trovaPrenotazioniAttive", e);
        }
        return out;
    }

    @Override
    public boolean inserisci(PrenotazioneBean bean) {
        if (bean == null || bean.getLibroId() == null || bean.getUtenteId() == null) return false;

        // evita duplicati (stessa utente, stesso libro, prenotazione attiva)
        String checkSql = "SELECT COUNT(*) FROM prenotazioni WHERE libro_id = ? AND utente_id = ? AND data_evasione IS NULL";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement check = c.prepareStatement(checkSql)) {
            check.setLong(1, bean.getLibroId());
            check.setLong(2, bean.getUtenteId());
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return false; // duplicata
                }
            }
        } catch (SQLException e) {
            return false;
        }

        String sql = "INSERT INTO prenotazioni (libro_id, utente_id, data_prenotazione, libro_titolo_snapshot, utente_snapshot) VALUES (?,?,?,?,?)";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setObject(1, bean.getLibroId(), java.sql.Types.BIGINT);
            ps.setObject(2, bean.getUtenteId(), java.sql.Types.BIGINT);
            LocalDate dp = bean.getDataPrenotazione() != null ? bean.getDataPrenotazione() : LocalDate.now();
            ps.setDate(3, java.sql.Date.valueOf(dp));
            if (bean.getLibroTitoloSnapshot() != null) ps.setString(4, bean.getLibroTitoloSnapshot()); else ps.setNull(4, Types.VARCHAR);
            if (bean.getUtenteSnapshot() != null) ps.setString(5, bean.getUtenteSnapshot()); else ps.setNull(5, Types.VARCHAR);

            int n = ps.executeUpdate();
            return n > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public boolean evadiPrenotazione(Long prenotazioneId, LocalDate dataEvasione) {
        if (prenotazioneId == null) return false;
        String sql = "UPDATE prenotazioni SET data_evasione = ? WHERE id = ? AND data_evasione IS NULL";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(LocalDate.now()));
            ps.setLong(2, prenotazioneId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public int contaPrenotazioniAttivePerLibro(Long libroId) {
        // ritorna un conteggio (o 0 come stub)
        return 0;
    }

    private Prenotazione mapRow(ResultSet rs) throws SQLException {
        Prenotazione p = new Prenotazione();
        p.setId(rs.getLong("id"));
        Object lid = rs.getObject("libro_id");
        p.setLibroId(lid != null ? ((Number) lid).longValue() : null);
        Object uid = rs.getObject("utente_id");
        p.setUtenteId(uid != null ? ((Number) uid).longValue() : null);
        Date dp = rs.getDate("data_prenotazione");
        p.setDataPrenotazione(dp != null ? dp.toLocalDate() : null);
        Date de = rs.getDate("data_evasione");
        p.setDataEvasione(de != null ? de.toLocalDate() : null);
        p.setLibroTitoloSnapshot(rs.getString("libro_titolo_snapshot"));
        p.setUtenteSnapshot(rs.getString("utente_snapshot"));
        return p;
    }
}
