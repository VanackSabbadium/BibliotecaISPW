package it.biblioteca.dao.jdbc;

import it.biblioteca.bean.PrestitoBean;
import it.biblioteca.dao.ConnectionProvider;
import it.biblioteca.dao.DatabaseConnectionProvider;
import it.biblioteca.dao.PrestitoDAO;
import it.biblioteca.entity.Prestito;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementazione JDBC per PrestitoDAO più tollerante allo schema:
 * - usa SELECT * e accede alle colonne solo se presenti (metadata)
 * - implementa inserisci/chiudiPrestito in modo robusto rispetto a colonne legacy.
 */
public class JdbcPrestitoDAO implements PrestitoDAO {
    private final ConnectionProvider cp;

    public JdbcPrestitoDAO() { this(new DatabaseConnectionProvider()); }
    public JdbcPrestitoDAO(ConnectionProvider cp) { this.cp = cp; }

    @Override
    public List<Prestito> trovaTutti() {
        String sql = "SELECT * FROM prestiti ORDER BY id DESC";
        List<Prestito> out = new ArrayList<>();
        try (Connection c = cp.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                out.add(mapFlexible(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore trovaTutti prestiti", e);
        }
        return out;
    }

    @Override
    public List<Prestito> trovaPrestitiAttivi() {
        String sql = "SELECT * FROM prestiti WHERE data_restituzione IS NULL ORDER BY id DESC";
        List<Prestito> out = new ArrayList<>();
        try (Connection c = cp.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                out.add(mapFlexible(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore trovaPrestitiAttivi", e);
        }
        return out;
    }

    @Override
    public boolean inserisci(PrestitoBean bean) {
        // validazione minima
        if (bean == null || bean.getLibroId() == null || bean.getUtenteId() == null) {
            return false;
        }
        LocalDate dp = (bean.getDataPrestito() != null) ? bean.getDataPrestito() : LocalDate.now();

        try (Connection c = cp.getConnection()) {
            Map<String, ColInfo> cols = getColumnsMeta(c, "prestiti");

            boolean hasUserSnap   = cols.containsKey("utente_snapshot");
            boolean hasBookSnap   = cols.containsKey("libro_titolo_snapshot");
            boolean hasLegacyUser = cols.containsKey("utente");
            boolean hasLegacyBook = cols.containsKey("libro");

            boolean reqLegacyUser = hasLegacyUser && cols.get("utente").notNull && !cols.get("utente").hasDefault;
            boolean reqLegacyBook = hasLegacyBook && cols.get("libro").notNull && !cols.get("libro").hasDefault;

            // Costruisco lista colonne dinamicamente
            List<String> insertCols = new ArrayList<>();
            insertCols.add("libro_id");
            insertCols.add("utente_id");
            insertCols.add("data_prestito");

            if (hasUserSnap)   insertCols.add("utente_snapshot");
            if (hasBookSnap)   insertCols.add("libro_titolo_snapshot");
            if (reqLegacyUser) insertCols.add("utente");
            if (reqLegacyBook) insertCols.add("libro");

            String placeholders = String.join(",", Collections.nCopies(insertCols.size(), "?"));
            String sql = "INSERT INTO prestiti (" + String.join(",", insertCols) + ") VALUES (" + placeholders + ")";

            try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                int i = 1;
                // obbligatorie
                ps.setObject(i++, bean.getLibroId(), java.sql.Types.BIGINT);
                ps.setObject(i++, bean.getUtenteId(), java.sql.Types.BIGINT);
                ps.setDate(i++, java.sql.Date.valueOf(dp));

                // colonne dinamiche nell'ordine costruito
                for (int k = 3; k < insertCols.size(); k++) {
                    String col = insertCols.get(k);
                    switch (col) {
                        case "utente_snapshot" -> ps.setString(i++, bean.getUtenteSnapshot());
                        case "libro_titolo_snapshot" -> ps.setString(i++, bean.getLibroTitoloSnapshot());
                        case "utente" -> ps.setString(i++, safeSnapshotUtente(bean));
                        case "libro" -> ps.setString(i++, safeSnapshotLibro(bean));
                        default -> ps.setObject(i++, null);
                    }
                }

                int n = ps.executeUpdate();
                return n > 0;
            }
        } catch (SQLException e) {
            // log per debug
            System.err.println("JdbcPrestitoDAO.inserisci() fallita: " + e.getMessage() + " (SQLState: " + e.getSQLState() + ")");
            return false;
        }
    }

    @Override
    public boolean chiudiPrestito(Long prestitoId, LocalDate dataRestituzione) {
        if (prestitoId == null) return false;
        String sql = "UPDATE prestiti SET data_restituzione = ? WHERE id = ? AND data_restituzione IS NULL";
        try (Connection c = cp.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(dataRestituzione != null ? dataRestituzione : LocalDate.now()));
            ps.setLong(2, prestitoId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    // ------------------------
    // Mapping flessibile in lettura
    // ------------------------
    private Prestito mapFlexible(ResultSet rs) throws SQLException {
        Prestito p = new Prestito();

        p.setId(getLongObj(rs, "id"));
        p.setLibroId(getLongObj(rs, "libro_id"));
        p.setUtenteId(getLongObj(rs, "utente_id"));

        // Snapshot utente: priorità a 'utente_snapshot', fallback 'utente'
        String utenteSnap = null;
        if (hasColumn(rs, "utente_snapshot")) {
            utenteSnap = rs.getString("utente_snapshot");
        } else if (hasColumn(rs, "utente")) {
            utenteSnap = rs.getString("utente");
        }
        p.setUtente(utenteSnap);

        // Snapshot titolo libro: priorità a 'libro_titolo_snapshot', fallback 'libro'
        String libroTitSnap = null;
        if (hasColumn(rs, "libro_titolo_snapshot")) {
            libroTitSnap = rs.getString("libro_titolo_snapshot");
        } else if (hasColumn(rs, "libro")) {
            libroTitSnap = rs.getString("libro");
        }
        p.setLibroTitoloSnapshot(libroTitSnap);

        p.setDataPrestito(getLocalDate(rs, "data_prestito"));
        p.setDataRestituzione(getLocalDate(rs, "data_restituzione"));

        return p;
    }

    // ------------------------
    // Helpers metadati/resultset
    // ------------------------
    private static boolean hasColumn(ResultSet rs, String col) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        int cols = md.getColumnCount();
        for (int i = 1; i <= cols; i++) {
            String label = md.getColumnLabel(i);
            String name = md.getColumnName(i);
            if (col.equalsIgnoreCase(label) || col.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    private static Long getLongObj(ResultSet rs, String col) throws SQLException {
        if (!hasColumn(rs, col)) return null;
        Object o = rs.getObject(col);
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).longValue();
        try {
            return Long.parseLong(String.valueOf(o));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static LocalDate getLocalDate(ResultSet rs, String col) throws SQLException {
        if (!hasColumn(rs, col)) return null;
        java.sql.Date d = rs.getDate(col);
        return d != null ? d.toLocalDate() : null;
    }

    private static class ColInfo {
        final boolean notNull;
        final boolean hasDefault;
        ColInfo(boolean notNull, boolean hasDefault) {
            this.notNull = notNull;
            this.hasDefault = hasDefault;
        }
    }

    private static Map<String, ColInfo> getColumnsMeta(Connection c, String tableName) throws SQLException {
        Map<String, ColInfo> map = new HashMap<>();
        DatabaseMetaData md = c.getMetaData();

        // Prova con il nome così com'è
        try (ResultSet rs = md.getColumns(null, null, tableName, null)) {
            while (rs.next()) {
                String name = rs.getString("COLUMN_NAME");
                int nullable = rs.getInt("NULLABLE");
                String defVal = rs.getString("COLUMN_DEF");
                boolean notNull = (nullable == DatabaseMetaData.columnNoNulls);
                boolean hasDefault = defVal != null;
                map.put(name.toLowerCase(), new ColInfo(notNull, hasDefault));
            }
        }
        if (map.isEmpty()) {
            try (ResultSet rs = md.getColumns(null, null, tableName.toUpperCase(), null)) {
                while (rs.next()) {
                    String name = rs.getString("COLUMN_NAME");
                    int nullable = rs.getInt("NULLABLE");
                    String defVal = rs.getString("COLUMN_DEF");
                    boolean notNull = (nullable == DatabaseMetaData.columnNoNulls);
                    boolean hasDefault = defVal != null;
                    map.put(name.toLowerCase(), new ColInfo(notNull, hasDefault));
                }
            }
        }
        return map;
    }

    private static String safeSnapshotUtente(PrestitoBean bean) {
        String s = bean.getUtenteSnapshot();
        return s != null ? s : "";
    }

    private static String safeSnapshotLibro(PrestitoBean bean) {
        String s = bean.getLibroTitoloSnapshot();
        return s != null ? s : "";
    }
}
