package it.biblioteca.dao;

public interface CredenzialiDAO {
    /**
     * Crea o aggiorna le credenziali per uno specifico utente (utente_id).
     * Se esiste già una riga per utente_id la aggiorna, altrimenti la inserisce.
     */
    boolean upsertCredenziali(Long utenteId, String username, String plainPassword, String role);

    /**
     * Rimuove le credenziali associate a un utente.
     */
    boolean rimuoviCredenziali(Long utenteId);

    /**
     * Restituisce lo username associato a un utente (o null se non esiste).
     */
    String getUsernameForUserId(Long utenteId);

    /**
     * Restituisce la password in chiaro (password_plain) per un utente;
     * null se non esiste o se non impostata. *Solo* per display Admin.
     */
    String getPasswordPlainForUserId(Long utenteId);

    /**
     * Cerca una riga credenziali tramite username.
     * Ritorna utente_id se trovata, null altrimenti.
     */
    Long findUserIdByUsername(String username);
}