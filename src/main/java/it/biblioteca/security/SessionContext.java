package it.biblioteca.security;

/**
 * Contesto di sessione applicativo (valori in memoria durante l'esecuzione).
 * Contiene helper per il ruolo corrente e informazioni dell'utente autenticato.
 */
public final class SessionContext {

    public enum AppRole { ADMIN, BIBLIOTECARIO, UTENTE }

    private static volatile AppRole currentRole = null;
    private static volatile Integer tessera = null;
    private static volatile Long userId = null; // id della tabella utenti (utente_id)
    private static volatile String authenticatedUsername = null;

    private SessionContext() {}

    // Role
    public static void setRole(AppRole role) { currentRole = role; }
    public static AppRole getRole() { return currentRole; }
    public static boolean isBibliotecario() { return currentRole == AppRole.BIBLIOTECARIO; }
    public static boolean isUtente() { return currentRole == AppRole.UTENTE; }
    public static boolean isAdmin() { return currentRole == AppRole.ADMIN; }

    // Tessera (numero tessera dell'utente)
    public static void setTessera(Integer tesseraNumero) { tessera = tesseraNumero; }
    public static Integer getTessera() { return tessera; }

    // UserId (id tabella utenti)
    public static void setUserId(Long id) { userId = id; }
    // alias mantenuto per nomenclature precedenti
    public static void setUtenteId(Long id) { setUserId(id); }
    public static Long getUserId() { return userId; }
    public static Long getUtenteId() { return getUserId(); }

    // Username autenticato (se presente)
    public static void setAuthenticatedUsername(String uname) { authenticatedUsername = uname; }
    public static String getAuthenticatedUsername() { return authenticatedUsername; }

    /**
     * Resetta completamente il contesto di sessione (usato ad esempio al logout / cambio utente).
     */
    public static void reset() {
        currentRole = null;
        tessera = null;
        userId = null;
        authenticatedUsername = null;
    }

    /**
     * Sinonimo per chiarezza del codice (opzionale).
     */
    public static void clear() {
        reset();
    }
}
