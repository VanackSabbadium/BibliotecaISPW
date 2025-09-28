package it.biblioteca.dao.jdbc;

import it.biblioteca.bean.PrestitoBean;
import it.biblioteca.dao.ConnectionProvider;
import it.biblioteca.dao.DatabaseConnectionProvider;
import it.biblioteca.dao.PrestitoDAO;
import it.biblioteca.entity.Prestito;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

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
        if (bean == null || bean.getLibroId() == null || bean.getUtenteId() == null) {
            return false;
        }
        LocalDate dp = (bean.getDataPrestito() != null) ? bean.getDataPrestito() : LocalDate.now();

        Connection c = null;
        boolean previousAutoCommit = true;
        try {
            c = cp.getConnection();
            previousAutoCommit = c.getAutoCommit();
            c.setAutoCommit(false);

            Map<String, ColInfo> cols = getColumnsMeta(c);

            boolean hasUserSnap   = cols.containsKey("utente_snapshot");
            boolean hasBookSnap   = cols.containsKey("libro_titolo_snapshot");
            boolean hasLegacyUser = cols.containsKey("utente");
            boolean hasLegacyBook = cols.containsKey("libro");

            boolean reqLegacyUser = hasLegacyUser && cols.get("utente").notNull && !cols.get("utente").hasDefault;
            boolean reqLegacyBook = hasLegacyBook && cols.get("libro").notNull && !cols.get("libro").hasDefault;

            String insertSql = "INSERT INTO prestiti (libro_id, utente_id, data_prestito) VALUES (?, ?, ?)";
            long generatedId;
            try (PreparedStatement ps = c.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setObject(1, bean.getLibroId(), Types.BIGINT);
                ps.setObject(2, bean.getUtenteId(), Types.BIGINT);
                ps.setDate(3, java.sql.Date.valueOf(dp));
                int updated = ps.executeUpdate();
                if (updated == 0) {
                    c.rollback();
                    return false;
                }
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk != null && gk.next()) {
                        generatedId = gk.getLong(1);
                    } else {
                        c.rollback();
                        return false;
                    }
                }
            }

            if (hasUserSnap) {
                String upd = "UPDATE prestiti SET utente_snapshot = ? WHERE id = ?";
                try (PreparedStatement ps = c.prepareStatement(upd)) {
                    ps.setString(1, bean.getUtenteSnapshot());
                    ps.setLong(2, generatedId);
                    ps.executeUpdate();
                }
            }
            if (hasBookSnap) {
                String upd = "UPDATE prestiti SET libro_titolo_snapshot = ? WHERE id = ?";
                try (PreparedStatement ps = c.prepareStatement(upd)) {
                    ps.setString(1, bean.getLibroTitoloSnapshot());
                    ps.setLong(2, generatedId);
                    ps.executeUpdate();
                }
            }

            if (reqLegacyUser || (hasLegacyUser && bean.getUtenteSnapshot() != null)) {
                String upd = "UPDATE prestiti SET utente = ? WHERE id = ?";
                try (PreparedStatement ps = c.prepareStatement(upd)) {
                    ps.setString(1, safeSnapshotUtente(bean));
                    ps.setLong(2, generatedId);
                    ps.executeUpdate();
                }
            }

            if (reqLegacyBook || (hasLegacyBook && bean.getLibroTitoloSnapshot() != null)) {
                String upd = "UPDATE prestiti SET libro = ? WHERE id = ?";
                try (PreparedStatement ps = c.prepareStatement(upd)) {
                    ps.setString(1, safeSnapshotLibro(bean));
                    ps.setLong(2, generatedId);
                    ps.executeUpdate();
                }
            }

            c.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("JdbcPrestitoDAO.inserisci() fallita: " + e.getMessage() +
                    " (SQLState: " + e.getSQLState() + ")");
            if (c != null) {
                try { c.rollback(); } catch (SQLException ex) { /* ignore */ }
            }
            return false;
        } finally {
            if (c != null) {
                try {
                    c.setAutoCommit(previousAutoCommit);
                    c.close();
                } catch (SQLException ignored) { }
            }
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

    private Prestito mapFlexible(ResultSet rs) throws SQLException {
        Prestito p = new Prestito();

        p.setId(getLongObj(rs, "id"));
        p.setLibroId(getLongObj(rs, "libro_id"));
        p.setUtenteId(getLongObj(rs, "utente_id"));

        String utenteSnap = null;
        if (hasColumn(rs, "utente_snapshot")) {
            utenteSnap = rs.getString("utente_snapshot");
        } else if (hasColumn(rs, "utente")) {
            utenteSnap = rs.getString("utente");
        }
        p.setUtente(utenteSnap);

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

    private static Map<String, ColInfo> getColumnsMeta(Connection c) throws SQLException {
        Map<String, ColInfo> map = new HashMap<>();
        DatabaseMetaData md = c.getMetaData();

        try (ResultSet rs = md.getColumns(null, null, "prestiti", null)) {
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
            try (ResultSet rs = md.getColumns(null, null, "prestiti".toUpperCase(), null)) {
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