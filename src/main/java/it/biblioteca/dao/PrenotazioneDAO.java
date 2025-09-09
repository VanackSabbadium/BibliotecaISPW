package it.biblioteca.dao;

import it.biblioteca.bean.PrenotazioneBean;
import it.biblioteca.entity.Prenotazione;

import java.time.LocalDate;
import java.util.List;

public interface PrenotazioneDAO {
    List<Prenotazione> trovaTutte();
    List<Prenotazione> trovaPrenotazioniAttive();
    boolean inserisci(PrenotazioneBean bean);
    boolean evadiPrenotazione(Long prenotazioneId, LocalDate dataEvasione);
    int contaPrenotazioniAttivePerLibro(Long libroId);
}
