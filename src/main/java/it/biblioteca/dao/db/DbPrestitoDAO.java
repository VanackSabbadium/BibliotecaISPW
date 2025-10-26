package it.biblioteca.dao.db;

import it.biblioteca.bean.PrestitoBean;
import it.biblioteca.dao.ConnectionProvider;
import it.biblioteca.dao.PrestitoDAO;
import it.biblioteca.entity.Prestito;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DbPrestitoDAO implements PrestitoDAO {

    private final ConnectionProvider cp;

    public DbPrestitoDAO(ConnectionProvider cp) {
        this.cp = cp;
    }

    @Override
    public List<Prestito> trovaTutti() {
        final String sql = """
                SELECT id,
                       libro_id,
                       utente_id,
                       utente_descrizione,
                       data_prestito,
                       data_restituzione,
                       libro_titolo_snapshot
                FROM prestiti
                ORDER BY id DESC
                """;

        List<Prestito> out = new ArrayList<>();

        try (Connection conn = cp.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                out.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new IllegalArgumentException("Errore trovaTutti prestiti", e);
        }

        return out;
    }

    @Override
    public List<Prestito> trovaPrestitiAttivi() {
        final String sql = """
                SELECT id,
                       libro_id,
                       utente_id,
                       utente_descrizione,
                       data_prestito,
                       data_restituzione,
                       libro_titolo_snapshot
                FROM prestiti
                WHERE data_restituzione IS NULL
                ORDER BY id DESC
                """;

        List<Prestito> out = new ArrayList<>();

        try (Connection conn = cp.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                out.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new IllegalArgumentException("Errore trovaPrestitiAttivi", e);
        }

        return out;
    }

    @Override
    public boolean inserisci(PrestitoBean bean) {
        final String sql = """
                INSERT INTO prestiti(
                    libro_id,
                    utente_id,
                    utente_descrizione,
                    data_prestito,
                    data_restituzione,
                    libro_isbn_snapshot,
                    libro_titolo_snapshot,
                    libro_autore_snapshot,
                    utente_nome_snapshot,
                    utente_cognome_snapshot,
                    utente_snapshot
                )
                VALUES (?,?,?,?,?,?,?,?,?,?,?)
                """;

        try (Connection conn = cp.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (bean.getLibroId() != null) ps.setLong(1, bean.getLibroId());
            else ps.setNull(1, Types.BIGINT);

            if (bean.getUtenteId() != null) ps.setLong(2, bean.getUtenteId());
            else ps.setNull(2, Types.BIGINT);

            ps.setString(3, bean.getUtenteSnapshot());

            LocalDate dp = bean.getDataPrestito();
            ps.setDate(4, dp != null ? Date.valueOf(dp) : null);

            ps.setNull(5, Types.DATE);

            ps.setNull(6, Types.VARCHAR);

            ps.setString(7, bean.getLibroTitoloSnapshot());

            ps.setNull(8, Types.VARCHAR);

            ps.setNull(9, Types.VARCHAR);

            ps.setNull(10, Types.VARCHAR);

            ps.setString(11, bean.getUtenteSnapshot());

            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            throw new IllegalArgumentException("Errore inserisci prestito", e);
        }
    }

    @Override
    public boolean chiudiPrestito(Long prestitoId, LocalDate dataRestituzione) {
        final String sql = "UPDATE prestiti SET data_restituzione=? WHERE id=?";
        try (Connection conn = cp.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, dataRestituzione != null ? Date.valueOf(dataRestituzione) : null);
            ps.setLong(2, prestitoId != null ? prestitoId : -1L);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalArgumentException("Errore chiudiPrestito", e);
        }
    }

    private Prestito mapRow(ResultSet rs) throws SQLException {
        Prestito p = new Prestito();

        Object idObj = rs.getObject("id");
        if (idObj instanceof Number n) p.setId(n.longValue());

        Object libroIdObj = rs.getObject("libro_id");
        if (libroIdObj instanceof Number nLibro) p.setLibroId(nLibro.longValue());

        Object utenteIdObj = rs.getObject("utente_id");
        if (utenteIdObj instanceof Number nUt) p.setUtenteId(nUt.longValue());

        p.setUtente(rs.getString("utente_descrizione"));

        Date dp = rs.getDate("data_prestito");
        p.setDataPrestito(dp != null ? dp.toLocalDate() : null);

        Date dr = rs.getDate("data_restituzione");
        p.setDataRestituzione(dr != null ? dr.toLocalDate() : null);

        p.setLibroTitoloSnapshot(rs.getString("libro_titolo_snapshot"));

        return p;
    }
}