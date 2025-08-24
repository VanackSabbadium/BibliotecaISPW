package it.biblioteca.dao;

import it.biblioteca.entity.Utente;

import java.util.List;

public interface UtenteDAO {
    List<Utente> trovaTutti();
    Utente trovaPerId(Long id);
    boolean inserisci(Utente u);
    boolean aggiorna(Utente u);
    boolean elimina(Long id);
}