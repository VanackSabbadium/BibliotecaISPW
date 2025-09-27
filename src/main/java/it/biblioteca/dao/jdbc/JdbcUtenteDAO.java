package it.biblioteca.dao.jdbc;

import it.biblioteca.dao.UtenteDAO;
import it.biblioteca.entity.Utente;

import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.security.MessageDigest;

public class JdbcUtenteDAO implements UtenteDAO {

    private String sha256Hex(String plain) {
        if (plain == null) return null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(plain.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private Connection getConn() throws SQLException {
        // ADATTARE se il tuo progetto espone la connessione in modo diverso
        return it.biblioteca.db.DatabaseConfig.getConnection();
    }

    @Override
    public List<Utente> trovaTutti() throws Exception {
        String sql = "SELECT u.id,u.tessera,u.nome,u.cognome,u.email,u.telefono,u.data_attivazione,u.data_scadenza," +
                " c.username, c.password_plain " +
                "FROM utenti u LEFT JOIN credenziali c ON u.id = c.utente_id";
        List<Utente> out = new ArrayList<>();
        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Utente u = mapRow(rs);
                out.add(u);
            }
        }
        return out;
    }

    @Override
    public Utente trovaPerId(Long id) throws Exception {
        String sql = "SELECT u.id,u.tessera,u.nome,u.cognome,u.email,u.telefono,u.data_attivazione,u.data_scadenza," +
                " c.username, c.password_plain " +
                "FROM utenti u LEFT JOIN credenziali c ON u.id = c.utente_id WHERE u.id = ?";
        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    @Override
    public boolean aggiungi(Utente u) throws Exception {
        String sql = "INSERT INTO utenti (tessera, nome, cognome, email, telefono, data_attivazione, data_scadenza) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setObject(1, u.getTessera());
            ps.setString(2, u.getNome());
            ps.setString(3, u.getCognome());
            ps.setString(4, u.getEmail());
            ps.setString(5, u.getTelefono());
            if (u.getDataAttivazione() != null) ps.setDate(6, Date.valueOf(u.getDataAttivazione())); else ps.setNull(6, Types.DATE);
            if (u.getDataScadenza() != null) ps.setDate(7, Date.valueOf(u.getDataScadenza())); else ps.setNull(7, Types.DATE);
            int rows = ps.executeUpdate();
            if (rows == 0) return false;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) u.setId(keys.getLong(1));
            }
            return true;
        }
    }

    @Override
    public boolean aggiorna(Utente u) throws Exception {
        String sql = "UPDATE utenti SET tessera=?, nome=?, cognome=?, email=?, telefono=?, data_attivazione=?, data_scadenza=? WHERE id=?";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, u.getTessera());
            ps.setString(2, u.getNome());
            ps.setString(3, u.getCognome());
            ps.setString(4, u.getEmail());
            ps.setString(5, u.getTelefono());
            if (u.getDataAttivazione() != null) ps.setDate(6, Date.valueOf(u.getDataAttivazione())); else ps.setNull(6, Types.DATE);
            if (u.getDataScadenza() != null) ps.setDate(7, Date.valueOf(u.getDataScadenza())); else ps.setNull(7, Types.DATE);
            ps.setLong(8, u.getId());
            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }

    @Override
    public boolean elimina(Long id) throws Exception {
        String sql = "DELETE FROM utenti WHERE id = ?";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }

    @Override
    public boolean creaCredenziali(Long utenteId, String username, String passwordPlain) throws Exception {
        if (utenteId == null || username == null || username.isBlank() || passwordPlain == null) return false;
        String hash = sha256Hex(passwordPlain);
        // Usare INSERT ... ON DUPLICATE KEY UPDATE per essere idempotente se esiste già
        String sql = "INSERT INTO credenziali (utente_id, username, password_hash, password_plain) VALUES (?, ?, ?, ?)" +
                " ON DUPLICATE KEY UPDATE username = VALUES(username), password_hash = VALUES(password_hash), password_plain = VALUES(password_plain)";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, utenteId);
            ps.setString(2, username);
            ps.setString(3, hash);
            ps.setString(4, passwordPlain);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException ex) {
            // Potrebbe fallire se la colonna password_plain non esiste. Proviamo fallback senza password_plain.
            if (ex.getErrorCode() == 1054 || ex.getSQLState().startsWith("42")) { // unknown column
                String sql2 = "INSERT INTO credenziali (utente_id, username, password_hash) VALUES (?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE username = VALUES(username), password_hash = VALUES(password_hash)";
                try (Connection c = getConn(); PreparedStatement ps2 = c.prepareStatement(sql2)) {
                    ps2.setLong(1, utenteId);
                    ps2.setString(2, username);
                    ps2.setString(3, hash);
                    int rows = ps2.executeUpdate();
                    return rows > 0;
                }
            } else {
                throw ex;
            }
        }
    }

    @Override
    public boolean aggiornaCredenziali(Long utenteId, String username, String passwordPlain) throws Exception {
        if (utenteId == null || username == null || username.isBlank()) return false;
        String hash = sha256Hex(passwordPlain);
        String sql = "UPDATE credenziali SET username = ?, password_hash = ?, password_plain = ? WHERE utente_id = ?";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, hash);
            ps.setString(3, passwordPlain);
            ps.setLong(4, utenteId);
            int rows = ps.executeUpdate();
            if (rows > 0) return true;
            return creaCredenziali(utenteId, username, passwordPlain);
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 1054 || ex.getSQLState().startsWith("42")) {
                String sql2 = "UPDATE credenziali SET username = ?, password_hash = ? WHERE utente_id = ?";
                try (Connection c = getConn(); PreparedStatement ps2 = c.prepareStatement(sql2)) {
                    ps2.setString(1, username);
                    ps2.setString(2, hash);
                    ps2.setLong(3, utenteId);
                    int rows = ps2.executeUpdate();
                    if (rows > 0) return true;
                    return creaCredenziali(utenteId, username, passwordPlain);
                }
            } else {
                throw ex;
            }
        }
    }

    @Override
    public Optional<String> getUsernameForUserId(Long utenteId) throws Exception {
        String sql = "SELECT username FROM credenziali WHERE utente_id = ?";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, utenteId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.ofNullable(rs.getString("username"));
            }
        }
        return Optional.empty();
    }

    private Utente mapRow(ResultSet rs) throws SQLException {
        Utente u = new Utente();
        u.setId(rs.getLong("id"));
        int tess = rs.getInt("tessera");
        if (!rs.wasNull()) u.setTessera(tess);
        u.setNome(rs.getString("nome"));
        u.setCognome(rs.getString("cognome"));
        u.setEmail(rs.getString("email"));
        u.setTelefono(rs.getString("telefono"));
        Date da = rs.getDate("data_attivazione");
        if (da != null) u.setDataAttivazione(da.toLocalDate());
        Date ds = rs.getDate("data_scadenza");
        if (ds != null) u.setDataScadenza(ds.toLocalDate());
        try {
            String username = rs.getString("username");
            u.setUsername(username);
        } catch (SQLException ignore) { /* colonna non presente */ }
        try {
            String pwd = rs.getString("password_plain");
            u.setPassword(pwd);
        } catch (SQLException ignore) { /* colonna non presente */ }
        return u;
    }
}
