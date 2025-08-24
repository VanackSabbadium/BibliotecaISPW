package it.biblioteca.controller;

import it.biblioteca.bean.UtenteBean;
import it.biblioteca.dao.UtenteDAO;
import it.biblioteca.entity.Utente;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class UtenteController {
    private final UtenteDAO utenteDAO;

    public UtenteController(UtenteDAO utenteDAO) {
        this.utenteDAO = utenteDAO;
    }

    public List<Utente> trovaTutti() {
        return utenteDAO.trovaTutti();
    }

    public List<Utente> trovaAttivi() {
        return utenteDAO.trovaTutti().stream().filter(this::isAttivo).collect(Collectors.toList());
    }

    public Utente trovaPerId(Long id) {
        if (id == null) return null;
        return utenteDAO.trovaPerId(id);
    }

    public boolean aggiungi(UtenteBean bean) {
        Utente u = fromBean(bean);
        // opzionale: default attivazione oggi se null
        if (u.getDataAttivazione() == null) u.setDataAttivazione(LocalDate.now());
        // validazioni base
        if (!validateDates(u.getDataAttivazione(), u.getDataScadenza())) return false;
        return utenteDAO.inserisci(u);
    }

    public boolean aggiorna(UtenteBean bean) {
        Utente u = fromBean(bean);
        if (u.getId() == null) return false;
        if (!validateDates(u.getDataAttivazione(), u.getDataScadenza())) return false;
        return utenteDAO.aggiorna(u);
    }

    public boolean elimina(Long id) {
        if (id == null) return false;
        return utenteDAO.elimina(id);
    }

    public boolean isAttivo(Utente u) {
        return u != null && u.isAttivo();
    }

    public boolean isAttivoById(Long id) {
        Utente u = utenteDAO.trovaPerId(id);
        return isAttivo(u);
    }

    private boolean validateDates(LocalDate att, LocalDate scad) {
        if (att != null && scad != null && scad.isBefore(att)) return false;
        return true;
    }

    private Utente fromBean(UtenteBean b) {
        Objects.requireNonNull(b, "UtenteBean nullo");
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