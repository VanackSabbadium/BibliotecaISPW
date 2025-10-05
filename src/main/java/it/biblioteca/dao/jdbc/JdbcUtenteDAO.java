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
    public List<Utente> trovaTutti() throws Exception {
        String sql = "SELECT id,tessera,nome,cognome,email,telefono,data_attivazione,data_scadenza FROM utenti ORDER BY cognome,nome";
        return query(sql, this::map);
    }

    @Override
    public Utente trovaPerId(Long id) throws Exception {
        String sql = "SELECT id,tessera,nome,cognome,email,telefono,data_attivazione,data_scadenza FROM utenti WHERE id=?";
        return queryOne(sql, ps -> ps.setLong(1, id), this::map);
    }

    @Override
    public boolean aggiungi(Utente u) throws Exception {
        String sql = "INSERT INTO utenti(tessera,nome,cognome,email,telefono,data_attivazione,data_scadenza) VALUES (?,?,?,?,?,?,?)";
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
    }

    @Override
    public boolean aggiorna(Utente u) throws Exception {
        String sql = "UPDATE utenti SET tessera=?,nome=?,cognome=?,email=?,telefono=?,data_attivazione=?,data_scadenza=? WHERE id=?";
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
    }

    @Override
    public boolean elimina(Long id) throws Exception {
        String sql = "DELETE FROM utenti WHERE id=?";
        return update(sql, ps -> ps.setLong(1, id)) > 0;
    }

    @Override
    public boolean creaCredenziali(Long utenteId, String username, String passwordPlain) throws Exception {
        String sql = "INSERT INTO credenziali(utente_id,username,password_hash) VALUES (?,?,SHA2(?,256))";
        return update(sql, ps -> {
            ps.setLong(1, utenteId);
            ps.setString(2, username);
            ps.setString(3, passwordPlain);
        }) > 0;
    }

    @Override
    public boolean aggiornaCredenziali(Long utenteId, String username, String passwordPlain) throws Exception {
        String sql = "INSERT INTO credenziali(utente_id,username,password_hash) VALUES (?,?,SHA2(?,256)) ON DUPLICATE KEY UPDATE username=VALUES(username), password_hash=VALUES(password_hash)";
        return update(sql, ps -> {
            ps.setLong(1, utenteId);
            ps.setString(2, username);
            ps.setString(3, passwordPlain);
        }) > 0;
    }

    @Override
    public Optional<String> getUsernameForUserId(Long utenteId) throws Exception {
        String sql = "SELECT username FROM credenziali WHERE utente_id=?";
        String u = queryOne(sql, ps -> ps.setLong(1, utenteId), rs -> rs.getString(1));
        return Optional.ofNullable(u);
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
}