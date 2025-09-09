package it.biblioteca.controller;

import it.biblioteca.bean.PrenotazioneBean;
import it.biblioteca.dao.PrenotazioneDAO;
import it.biblioteca.entity.Prenotazione;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class PrenotazioneController {

    private final PrenotazioneDAO prenotazioneDAO;

    public PrenotazioneController(PrenotazioneDAO prenotazioneDAO) {
        this.prenotazioneDAO = Objects.requireNonNull(prenotazioneDAO);
    }

    public enum Esito {
        OK,
        ERRORE_INSERIMENTO,
        UTENTE_NON_VALIDO,
        LIBRO_NON_VALIDO
    }

    public List<Prenotazione> trovaTutte() {
        return prenotazioneDAO.trovaTutte();
    }

    public List<Prenotazione> trovaPrenotazioniAttive() {
        return prenotazioneDAO.trovaPrenotazioniAttive();
    }

    public Esito registraPrenotazione(PrenotazioneBean bean) {
        if (bean == null) return Esito.ERRORE_INSERIMENTO;
        if (bean.getUtenteId() == null) return Esito.UTENTE_NON_VALIDO;
        if (bean.getLibroId() == null) return Esito.LIBRO_NON_VALIDO;

        boolean ok = prenotazioneDAO.inserisci(bean);
        return ok ? Esito.OK : Esito.ERRORE_INSERIMENTO;
    }

    public boolean evadiPrenotazione(Long prenId, LocalDate dataEvasione) {
        return prenotazioneDAO.evadiPrenotazione(prenId, dataEvasione);
    }

    public int contaPrenotazioniAttivePerLibro(Long libroId) {
        return prenotazioneDAO.contaPrenotazioniAttivePerLibro(libroId);
    }
}
