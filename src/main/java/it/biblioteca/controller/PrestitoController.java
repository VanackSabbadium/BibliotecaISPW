package it.biblioteca.controller;

import it.biblioteca.bean.PrestitoBean;
import it.biblioteca.dao.PrestitoDAO;
import it.biblioteca.entity.Prestito;

import java.time.LocalDate;
import java.util.List;

public class PrestitoController {
    private final PrestitoDAO prestitoDAO;
    private final UtenteController utenteController; // per la verifica attivo

    public enum Esito {
        OK,
        UTENTE_INATTIVO,
        ERRORE_INSERIMENTO
    }

    public PrestitoController(PrestitoDAO prestitoDAO, UtenteController utenteController) {
        this.prestitoDAO = prestitoDAO;
        this.utenteController = utenteController;
    }

    public List<Prestito> trovaTutti() {
        return prestitoDAO.trovaTutti();
    }

    public List<Prestito> trovaPrestitiAttivi() {
        return prestitoDAO.trovaPrestitiAttivi();
    }

    public Esito registraPrestito(PrestitoBean bean) {
        Long utenteId = bean.getUtenteId();
        if (utenteId == null || !utenteController.isAttivoById(utenteId)) {
            return Esito.UTENTE_INATTIVO;
        }
        boolean inserted = prestitoDAO.inserisci(bean);
        return inserted ? Esito.OK : Esito.ERRORE_INSERIMENTO;
    }

    public boolean registraRestituzione(Long prestitoId, LocalDate dataRestituzione) {
        return prestitoDAO.chiudiPrestito(prestitoId, dataRestituzione);
    }

    public boolean esistonoPrestitiAttiviPerLibro(Long libroId) {
        if (libroId == null) return false;
        return prestitoDAO.trovaPrestitiAttivi()
                .stream()
                .anyMatch(p -> libroId.equals(p.getLibroId()));
    }
}