package it.biblioteca.dao;

import it.biblioteca.entity.Utente;

import java.util.List;
import java.util.Optional;

public interface UtenteDAO {
    List<Utente> trovaTutti() throws Exception;
    Utente trovaPerId(Long id) throws Exception;
    boolean aggiungi(Utente u) throws Exception;
    boolean aggiorna(Utente u) throws Exception;
    boolean elimina(Long id) throws Exception;

    boolean creaCredenziali(Long utenteId, String username, String passwordPlain) throws Exception;

    boolean aggiornaCredenziali(Long utenteId, String username, String passwordPlain) throws Exception;

    Optional<String> getUsernameForUserId(Long utenteId) throws Exception;
}
