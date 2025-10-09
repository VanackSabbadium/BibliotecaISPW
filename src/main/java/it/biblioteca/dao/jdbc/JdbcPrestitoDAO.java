package it.biblioteca.dao.jdbc;

import it.biblioteca.bean.PrestitoBean;
import it.biblioteca.dao.ConnectionProvider;
import it.biblioteca.dao.PrestitoDAO;
import it.biblioteca.entity.Prestito;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class JdbcPrestitoDAO extends JdbcSupport implements PrestitoDAO {
    public JdbcPrestitoDAO(ConnectionProvider cp) {
        super(cp);
    }

    @Override
    public List<Prestito> trovaTutti() {
        String sql = "SELECT id,libro_id,utente_id,utente_snapshot,libro_titolo_snapshot,data_prestito,data_restituzione FROM prestiti ORDER BY id DESC";
        try {
            return query(sql, this::map);
        } catch (SQLException e) {
            throw new IllegalArgumentException("Errore trovaTutti prestiti", e);
        }
    }

    @Override
    public List<Prestito> trovaPrestitiAttivi() {
        String sql = "SELECT id,libro_id,utente_id,utente_snapshot,libro_titolo_snapshot,data_prestito,data_restituzione FROM prestiti WHERE data_restituzione IS NULL ORDER BY id DESC";
        try {
            return query(sql, this::map);
        } catch (SQLException e) {
            throw new IllegalArgumentException("Errore trovaPrestitiAttivi", e);
        }
    }

    @Override
    public boolean inserisci(PrestitoBean bean) {
        if (bean == null || bean.getLibroId() == null || bean.getUtenteId() == null) return false;
        LocalDate dp = bean.getDataPrestito() != null ? bean.getDataPrestito() : LocalDate.now();
        String sql = "INSERT INTO prestiti(libro_id,utente_id,data_prestito,utente_snapshot,libro_titolo_snapshot) VALUES (?,?,?,?,?)";
        try {
            long id = insertAndReturnKey(sql, ps -> {
                ps.setObject(1, bean.getLibroId(), java.sql.Types.BIGINT);
                ps.setObject(2, bean.getUtenteId(), java.sql.Types.BIGINT);
                ps.setDate(3, Date.valueOf(dp));
                ps.setString(4, bean.getUtenteSnapshot());
                ps.setString(5, bean.getLibroTitoloSnapshot());
            });
            return id > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public boolean chiudiPrestito(Long prestitoId, LocalDate dataRestituzione) {
        if (prestitoId == null) return false;
        String sql = "UPDATE prestiti SET data_restituzione=? WHERE id=? AND data_restituzione IS NULL";
        try {
            return update(sql, ps -> {
                ps.setDate(1, Date.valueOf(dataRestituzione != null ? dataRestituzione : LocalDate.now()));
                ps.setLong(2, prestitoId);
            }) > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    private Prestito map(ResultSet rs) throws SQLException {
        Prestito p = new Prestito();
        Object idObj = rs.getObject("id");
        p.setId(idObj == null ? null : ((Number) idObj).longValue());
        Object lid = rs.getObject("libro_id");
        p.setLibroId(lid == null ? null : ((Number) lid).longValue());
        Object uid = rs.getObject("utente_id");
        p.setUtenteId(uid == null ? null : ((Number) uid).longValue());
        p.setUtente(rs.getString("utente_snapshot"));
        p.setLibroTitoloSnapshot(rs.getString("libro_titolo_snapshot"));
        Date dp = rs.getDate("data_prestito");
        p.setDataPrestito(dp != null ? dp.toLocalDate() : null);
        Date dr = rs.getDate("data_restituzione");
        p.setDataRestituzione(dr != null ? dr.toLocalDate() : null);
        return p;
    }
}