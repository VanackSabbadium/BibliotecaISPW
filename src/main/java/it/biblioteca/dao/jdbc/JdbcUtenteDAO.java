package it.biblioteca.dao.jdbc;

import it.biblioteca.dao.ConnectionProvider;
import it.biblioteca.dao.UtenteDAO;
import it.biblioteca.entity.Utente;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class JdbcUtenteDAO extends JdbcSupport implements UtenteDAO {
    public JdbcUtenteDAO(ConnectionProvider cp) {
        super(cp);
    }

    @Override
    public List<Utente> trovaTutti() {
        String sql = "SELECT id,tessera,nome,cognome,email,telefono,data_attivazione,data_scadenza FROM utenti ORDER BY cognome,nome";
        try {
            return query(sql, this::map);
        } catch (SQLException e) {
            throw new IllegalArgumentException("Errore trovaTutti utenti", e);
        }
    }

    @Override
    public Utente trovaPerId(Long id) {
        String sql = "SELECT id,tessera,nome,cognome,email,telefono,data_attivazione,data_scadenza FROM utenti WHERE id=?";
        try {
            return queryOne(sql, ps -> ps.setLong(1, id), this::map);
        } catch (SQLException e) {
            throw new IllegalArgumentException("Errore trovaPerId utente", e);
        }
    }

    @Override
    public boolean aggiungi(Utente u) {
        String sql = "INSERT INTO utenti(tessera,nome,cognome,email,telefono,data_attivazione,data_scadenza) VALUES (?,?,?,?,?,?,?)";
        try {
            long id = insertAndReturnKey(sql, ps -> {
                ps.setInt(1, u.getTessera());
                ps.setString(2, u.getNome());
                ps.setString(3, u.getCognome());
                ps.setString(4, u.getEmail());
                ps.setString(5, u.getTelefono());
                LocalDate da = u.getDataAttivazione();
                LocalDate ds = u.getDataScadenza();
                ps.setDate(6, da != null ? Date.valueOf(da) : null);
                ps.setDate(7, ds != null ? Date.valueOf(ds) : null);
            });
            if (id > 0) u.setId(id);
            return id > 0;
        } catch (SQLException e) {
            throw new IllegalArgumentException("Errore aggiungi utente", e);
        }
    }

    @Override
    public boolean aggiorna(Utente u) {
        String sql = "UPDATE utenti SET tessera=?,nome=?,cognome=?,email=?,telefono=?,data_attivazione=?,data_scadenza=? WHERE id=?";
        try {
            return update(sql, ps -> {
                ps.setInt(1, u.getTessera());
                ps.setString(2, u.getNome());
                ps.setString(3, u.getCognome());
                ps.setString(4, u.getEmail());
                ps.setString(5, u.getTelefono());
                LocalDate da = u.getDataAttivazione();
                LocalDate ds = u.getDataScadenza();
                ps.setDate(6, da != null ? Date.valueOf(da) : null);
                ps.setDate(7, ds != null ? Date.valueOf(ds) : null);
                ps.setLong(8, u.getId());
            }) > 0;
        } catch (SQLException e) {
            throw new IllegalArgumentException("Errore aggiorna utente", e);
        }
    }

    @Override
    public boolean elimina(Long id) {
        String sql = "DELETE FROM utenti WHERE id=?";
        try {
            return update(sql, ps -> ps.setLong(1, id)) > 0;
        } catch (SQLException e) {
            throw new IllegalArgumentException("Errore elimina utente", e);
        }
    }

    @Override
    public boolean creaCredenziali(Long utenteId, String username, String passwordPlain) {
        String sql = "INSERT INTO credenziali(utente_id,username,password_hash) VALUES (?,?,SHA2(?,256))";
        try {
            return update(sql, ps -> {
                ps.setLong(1, utenteId);
                ps.setString(2, username);
                ps.setString(3, passwordPlain);
            }) > 0;
        } catch (SQLException e) {
            throw new IllegalArgumentException("Errore creaCredenziali", e);
        }
    }

    @Override
    public boolean aggiornaCredenziali(Long utenteId, String username, String passwordPlain) {
        String sql = "INSERT INTO credenziali(utente_id,username,password_hash) VALUES (?,?,SHA2(?,256)) " +
                "ON DUPLICATE KEY UPDATE username=VALUES(username), password_hash=VALUES(password_hash)";
        try {
            return update(sql, ps -> {
                ps.setLong(1, utenteId);
                ps.setString(2, username);
                ps.setString(3, passwordPlain);
            }) > 0;
        } catch (SQLException e) {
            throw new IllegalArgumentException("Errore aggiornaCredenziali", e);
        }
    }

    @Override
    public Optional<String> getUsernameForUserId(Long utenteId) {
        String sql = "SELECT username FROM credenziali WHERE utente_id=?";
        try {
            String u = queryOne(sql, ps -> ps.setLong(1, utenteId), rs -> rs.getString(1));
            return Optional.ofNullable(u);
        } catch (SQLException e) {
            throw new IllegalArgumentException("Errore getUsernameForUserId", e);
        }
    }

    @Override
    public Optional<AuthData> findAuthByUsername(String username) {
        String sql = """
                SELECT c.username, c.password_hash, c.role, u.id AS user_id, u.tessera AS tessera
                FROM credenziali c
                JOIN utenti u ON u.id = c.utente_id
                WHERE c.username = ?
                """;
        try {
            AuthData row = queryOne(sql, ps -> ps.setString(1, username), rs -> new AuthData(
                    rs.getString("username"),
                    rs.getString("password_hash"),
                    rs.getString("role"),
                    asLong(rs),
                    asInteger(rs)
            ));
            return Optional.ofNullable(row);
        } catch (SQLException e) {
            throw new IllegalArgumentException("Errore findAuthByUsername", e);
        }
    }

    private Utente map(ResultSet rs) throws SQLException {
        Utente u = new Utente();
        Object idObj = rs.getObject("id");
        u.setId(idObj == null ? null : ((Number) idObj).longValue());
        u.setTessera(rs.getInt("tessera"));
        u.setNome(rs.getString("nome"));
        u.setCognome(rs.getString("cognome"));
        u.setEmail(rs.getString("email"));
        u.setTelefono(rs.getString("telefono"));
        Date da = rs.getDate("data_attivazione");
        Date ds = rs.getDate("data_scadenza");
        u.setDataAttivazione(da != null ? da.toLocalDate() : null);
        u.setDataScadenza(ds != null ? ds.toLocalDate() : null);
        return u;
    }

    private static Long asLong(ResultSet rs) throws SQLException {
        Object v = rs.getObject("user_id");
        return v == null ? null : ((Number) v).longValue();
    }

    private static Integer asInteger(ResultSet rs) throws SQLException {
        Object v = rs.getObject("tessera");
        return v == null ? null : ((Number) v).intValue();
    }
}