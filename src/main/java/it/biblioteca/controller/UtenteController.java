package it.biblioteca.controller;

import it.biblioteca.bean.UtenteBean;
import it.biblioteca.dao.UtenteDAO;
import it.biblioteca.entity.Utente;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller per Utente: wrapper sul DAO, con conversioni da/verso UtenteBean
 * e helper utili per la UI.
 */
public class UtenteController {

    private final UtenteDAO utenteDAO;
    // campo opzionale: molte parti del progetto passano anche un credenzialiDAO al costruttore;
    // lo memorizziamo ma non è obbligatorio usarlo qui.
    private final Object credenzialiDAO; // typed Object per evitare dipendenze forti; non è obbligatorio

    // costruttore principale (compatibile con chiamate che passano solo UtenteDAO)
    public UtenteController(UtenteDAO utenteDAO) {
        this(utenteDAO, null);
    }

    // costruttore overload per compatibilità con chiamate che passano (utenteDAO, credenzialiDAO)
    public UtenteController(UtenteDAO utenteDAO, Object credenzialiDAO) {
        this.utenteDAO = utenteDAO;
        this.credenzialiDAO = credenzialiDAO;
    }

    // -------------------------
    // Operazioni base (entity)
    // -------------------------
    public List<Utente> trovaTutti() {
        try {
            return utenteDAO.trovaTutti();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Utente trovaPerId(Long id) {
        try {
            return utenteDAO.trovaPerId(id);
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

    // -------------------------
    // Overload per UtenteBean (usati dalla UI)
    // -------------------------
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

    // conversione bean -> entity (mappa campi comuni)
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
        // username/password non impostati qui: gestiti dall'Admin con metodi specifici
        return u;
    }

    // -------------------------
    // Helper utili alla UI / PrestitoController
    // -------------------------
    /**
     * Ritorna true se l'utente con id dato è "attivo" (dataScadenza nulla o >= oggi).
     */
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

    /**
     * Ritorna la lista degli utenti attivi (per SelectUserDialog ecc.)
     */
    public List<Utente> trovaAttivi() {
        List<Utente> all = trovaTutti();
        return all.stream()
                .filter(u -> {
                    LocalDate scad = u.getDataScadenza();
                    return scad == null || !scad.isBefore(LocalDate.now());
                }).collect(Collectors.toList());
    }

    // -------------------------
    // Funzioni credenziali (Admin) - delegano al DAO
    // -------------------------
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

    public boolean rimuoviCredenziali(Long utenteId) {
        try {
            return utenteDAO.rimuoviCredenziali(utenteId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<Utente> findByUsername(String username) {
        try {
            return utenteDAO.findByUsername(username);
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
