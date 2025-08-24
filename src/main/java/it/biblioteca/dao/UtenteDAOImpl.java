package it.biblioteca.dao;

import it.biblioteca.config.DatabaseConfig;
import it.biblioteca.entity.Utente;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UtenteDAOImpl implements UtenteDAO {
    private final DatabaseConfig db;

    public UtenteDAOImpl(DatabaseConfig db) {
        this.db = db;
    }

    @Override
    public List<Utente> trovaTutti() {
        String sql = "SELECT id, tessera, nome, cognome, email, telefono, data_attivazione, data_scadenza FROM utenti";
        List<Utente> out = new ArrayList<>();
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Errore trovaTutti utenti", e);
        }
        return out;
    }

    @Override
    public Utente trovaPerId(Long id) {
        String sql = "SELECT id, tessera, nome, cognome, email, telefono, data_attivazione, data_scadenza FROM utenti WHERE id = ?";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore trovaPerId utente", e);
        }
        return null;
    }

    @Override
    public boolean inserisci(Utente u) {
        String sql = "INSERT INTO utenti (tessera, nome, cognome, email, telefono, data_attivazione, data_scadenza) VALUES (?,?,?,?,?,?,?)";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setObject(1, u.getTessera(), Types.INTEGER);
            ps.setString(2, u.getNome());
            ps.setString(3, u.getCognome());
            ps.setString(4, u.getEmail());
            ps.setString(5, u.getTelefono());
            if (u.getDataAttivazione() != null) ps.setDate(6, Date.valueOf(u.getDataAttivazione())); else ps.setNull(6, Types.DATE);
            if (u.getDataScadenza() != null) ps.setDate(7, Date.valueOf(u.getDataScadenza())); else ps.setNull(7, Types.DATE);
            int n = ps.executeUpdate();
            if (n > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) u.setId(keys.getLong(1));
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            // Probabile vincolo di unicità su tessera/email
            return false;
        }
    }

    @Override
    public boolean aggiorna(Utente u) {
        String sql = "UPDATE utenti SET tessera=?, nome=?, cognome=?, email=?, telefono=?, data_attivazione=?, data_scadenza=? WHERE id=?";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, u.getTessera(), Types.INTEGER);
            ps.setString(2, u.getNome());
            ps.setString(3, u.getCognome());
            ps.setString(4, u.getEmail());
            ps.setString(5, u.getTelefono());
            if (u.getDataAttivazione() != null) ps.setDate(6, Date.valueOf(u.getDataAttivazione())); else ps.setNull(6, Types.DATE);
            if (u.getDataScadenza() != null) ps.setDate(7, Date.valueOf(u.getDataScadenza())); else ps.setNull(7, Types.DATE);
            ps.setLong(8, u.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public boolean elimina(Long id) {
        String sql = "DELETE FROM utenti WHERE id = ?";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    private Utente map(ResultSet rs) throws SQLException {
        Utente u = new Utente();
        u.setId(rs.getLong("id"));
        u.setTessera((Integer) rs.getObject("tessera"));
        u.setNome(rs.getString("nome"));
        u.setCognome(rs.getString("cognome"));
        u.setEmail(rs.getString("email"));
        u.setTelefono(rs.getString("telefono"));
        Date dA = rs.getDate("data_attivazione");
        Date dS = rs.getDate("data_scadenza");
        u.setDataAttivazione(dA != null ? dA.toLocalDate() : null);
        u.setDataScadenza(dS != null ? dS.toLocalDate() : null);
        return u;
    }
}