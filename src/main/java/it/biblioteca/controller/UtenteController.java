package it.biblioteca.controller;

import it.biblioteca.bean.UtenteBean;
import it.biblioteca.dao.UtenteDAO;
import it.biblioteca.entity.Utente;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UtenteController {

    private final UtenteDAO utenteDAO;

    private final Object credenzialiDAO;

    public UtenteController(UtenteDAO utenteDAO, Object credenzialiDAO) {
        this.utenteDAO = utenteDAO;
        this.credenzialiDAO = credenzialiDAO;
    }

    public List<Utente> trovaTutti() {
        try {
            return utenteDAO.trovaTutti();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean aggiungi(Utente u) {
        try {
            return utenteDAO.aggiungi(u);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean aggiorna(Utente u) {
        try {
            return utenteDAO.aggiorna(u);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean elimina(Long id) {
        try {
            return utenteDAO.elimina(id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean aggiungi(UtenteBean bean) {
        if (bean == null) return false;
        Utente u = beanToEntity(bean);
        return aggiungi(u);
    }

    public boolean aggiorna(UtenteBean bean) {
        if (bean == null) return false;
        Utente u = beanToEntity(bean);
        return aggiorna(u);
    }

    private Utente beanToEntity(UtenteBean b) {
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

    public boolean isAttivoById(Long id) {
        if (id == null) return false;
        try {
            Utente u = utenteDAO.trovaPerId(id);
            if (u == null) return false;
            LocalDate scad = u.getDataScadenza();
            return scad == null || !scad.isBefore(LocalDate.now());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Utente> trovaAttivi() {
        List<Utente> all = trovaTutti();
        return all.stream()
                .filter(u -> {
                    LocalDate scad = u.getDataScadenza();
                    return scad == null || !scad.isBefore(LocalDate.now());
                }).collect(Collectors.toList());
    }

    public boolean creaCredenziali(Long utenteId, String username, String passwordPlain) {
        try {
            return utenteDAO.creaCredenziali(utenteId, username, passwordPlain);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean aggiornaCredenziali(Long utenteId, String username, String passwordPlain) {
        try {
            return utenteDAO.aggiornaCredenziali(utenteId, username, passwordPlain);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<String> getUsernameForUserId(Long utenteId) {
        try {
            return utenteDAO.getUsernameForUserId(utenteId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
