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

    // --- metodi per gestione credenziali (Admin) ---
    /**
     * Crea le credenziali per l'utente indicato.
     * Inserisce username + password_hash (+ opzionale password_plain).
     */
    boolean creaCredenziali(Long utenteId, String username, String passwordPlain) throws Exception;

    /**
     * Aggiorna le credenziali esistenti associate all'utente.
     */
    boolean aggiornaCredenziali(Long utenteId, String username, String passwordPlain) throws Exception;

    /**
     * Rimuove le credenziali associate all'utente.
     */
    boolean rimuoviCredenziali(Long utenteId) throws Exception;

    /**
     * Restituisce l'Utente associato all'username (compresi campo username/password se presenti).
     */
    Optional<Utente> findByUsername(String username) throws Exception;

    /**
     * Restituisce username (se esiste) per il dato utente_id
     */
    Optional<String> getUsernameForUserId(Long utenteId) throws Exception;
}
