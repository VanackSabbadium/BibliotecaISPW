package it.biblioteca.service;

import it.biblioteca.bean.UtenteBean;
import it.biblioteca.dao.UtenteDAO;
import it.biblioteca.entity.Utente;
import it.biblioteca.events.EventBus;
import it.biblioteca.events.events.UtenteChanged;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class UtenteService {
    private final UtenteDAO utenteDAO;

    public UtenteService(UtenteDAO utenteDAO) {
        this.utenteDAO = utenteDAO;
    }

    public List<Utente> findAll() {
        try {
            return utenteDAO.trovaTutti();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public List<Utente> findActive() {
        return findAll().stream().filter(this::isActive).toList();
    }

    public boolean add(UtenteBean bean) {
        try {
            Utente u = toEntity(bean);
            boolean ok = utenteDAO.aggiungi(u);
            if (ok) EventBus.getDefault().publish(new UtenteChanged(UtenteChanged.Action.ADDED, u.getId()));
            return ok;
        } catch (Exception _) {
            return false;
        }
    }

    public boolean update(UtenteBean bean) {
        try {
            Utente u = toEntity(bean);
            boolean ok = utenteDAO.aggiorna(u);
            if (ok) EventBus.getDefault().publish(new UtenteChanged(UtenteChanged.Action.UPDATED, u.getId()));
            return ok;
        } catch (Exception _) {
            return false;
        }
    }

    public boolean delete(Long id) {
        try {
            boolean ok = utenteDAO.elimina(id);
            if (ok) EventBus.getDefault().publish(new UtenteChanged(UtenteChanged.Action.DELETED, id));
            return ok;
        } catch (Exception _) {
            return false;
        }
    }

    public boolean createCredentials(Long utenteId, String username, String passwordPlain) {
        try {
            boolean ok = utenteDAO.creaCredenziali(utenteId, username, passwordPlain);
            if (ok) EventBus.getDefault().publish(new UtenteChanged(UtenteChanged.Action.CREDENTIALS_CHANGED, utenteId));
            return ok;
        } catch (Exception _) {
            return false;
        }
    }

    public boolean updateCredentials(Long utenteId, String username, String passwordPlain) {
        try {
            boolean ok = utenteDAO.aggiornaCredenziali(utenteId, username, passwordPlain);
            if (ok) EventBus.getDefault().publish(new UtenteChanged(UtenteChanged.Action.CREDENTIALS_CHANGED, utenteId));
            return ok;
        } catch (Exception _) {
            return false;
        }
    }

    public Optional<String> getUsernameForUserId(Long utenteId) {
        try {
            return utenteDAO.getUsernameForUserId(utenteId);
        } catch (Exception _) {
            return Optional.empty();
        }
    }

    private boolean isActive(Utente u) {
        LocalDate scad = u.getDataScadenza();
        return scad == null || !scad.isBefore(LocalDate.now());
    }

    private Utente toEntity(UtenteBean b) {
        Utente u = new Utente();
        u.setId(b.getId());
        u.setTessera(b.getTessera());
        u.setNome(b.getNome());
        u.setCognome(b.getCognome());
        u.setEmail(b.getEmail());
        u.setTelefono(b.getTelefono());
        u.setDataAttivazione(b.getDataAttivazione());
        u.setDataScadenza(b.getDataScadenza());
        return u;
    }
}