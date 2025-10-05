package it.biblioteca.service;

import it.biblioteca.bean.PrestitoBean;
import it.biblioteca.controller.PrestitoController;
import it.biblioteca.dao.BookDAO;
import it.biblioteca.dao.PrestitoDAO;
import it.biblioteca.dao.UtenteDAO;
import it.biblioteca.entity.Prestito;
import it.biblioteca.entity.Utente;
import it.biblioteca.events.EventBus;
import it.biblioteca.events.events.PrestitoChanged;

import java.time.LocalDate;
import java.util.List;

public class PrestitoService {
    private final PrestitoDAO prestitoDAO;
    private final BookDAO bookDAO;
    private final UtenteDAO utenteDAO;

    public PrestitoService(PrestitoDAO prestitoDAO, BookDAO bookDAO, UtenteDAO utenteDAO) {
        this.prestitoDAO = prestitoDAO;
        this.bookDAO = bookDAO;
        this.utenteDAO = utenteDAO;
    }

    public List<Prestito> findAll() {
        return prestitoDAO.trovaTutti();
    }

    public List<Prestito> findActive() {
        return prestitoDAO.trovaPrestitiAttivi();
    }

    public PrestitoController.Esito registerLoan(PrestitoBean bean) {
        try {
            if (bean == null || bean.getUtenteId() == null || bean.getLibroId() == null) return PrestitoController.Esito.ERRORE_INSERIMENTO;
            Utente u = utenteDAO.trovaPerId(bean.getUtenteId());
            if (u == null) return PrestitoController.Esito.ERRORE_INSERIMENTO;
            LocalDate scad = u.getDataScadenza();
            if (scad != null && scad.isBefore(LocalDate.now())) return PrestitoController.Esito.UTENTE_INATTIVO;
            boolean ok = prestitoDAO.inserisci(bean);
            if (ok) EventBus.getDefault().publish(new PrestitoChanged(PrestitoChanged.Action.REGISTERED, null));
            return ok ? PrestitoController.Esito.OK : PrestitoController.Esito.ERRORE_INSERIMENTO;
        } catch (Exception e) {
            return PrestitoController.Esito.ERRORE_INSERIMENTO;
        }
    }

    public boolean registerReturn(Long prestitoId, LocalDate data) {
        boolean ok = prestitoDAO.chiudiPrestito(prestitoId, data);
        if (ok) EventBus.getDefault().publish(new PrestitoChanged(PrestitoChanged.Action.RETURNED, prestitoId));
        return ok;
    }
}
