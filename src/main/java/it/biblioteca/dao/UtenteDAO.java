package it.biblioteca.dao;

import it.biblioteca.entity.Utente;

import java.util.List;
import java.util.Optional;

public interface UtenteDAO {

    List<Utente> trovaTutti();
    Utente trovaPerId(Long id);
    boolean aggiungi(Utente u);
    boolean aggiorna(Utente u);
    boolean elimina(Long id);

    boolean creaCredenziali(Long utenteId, String username, String passwordPlain);
    boolean aggiornaCredenziali(Long utenteId, String username, String passwordPlain);
    Optional<String> getUsernameForUserId(Long utenteId);

    record AuthData(String username, String passwordHash, String role, Long userId, Integer tessera) {}

    Optional<AuthData> findAuthByUsername(String username);
}