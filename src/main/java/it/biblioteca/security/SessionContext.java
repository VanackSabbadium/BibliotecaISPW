package it.biblioteca.security;

public final class SessionContext {

    public enum AppRole { ADMIN, BIBLIOTECARIO, UTENTE }

    private static volatile AppRole currentRole = null;
    private static volatile Integer tessera = null;
    private static volatile Long userId = null;
    private static volatile String authenticatedUsername = null;

    public static void setRole(AppRole role) { currentRole = role; }
    public static boolean isBibliotecario() { return currentRole == AppRole.BIBLIOTECARIO; }
    public static boolean isUtente() { return currentRole == AppRole.UTENTE; }
    public static boolean isAdmin() { return currentRole == AppRole.ADMIN; }

    public static void setTessera(Integer tesseraNumero) { tessera = tesseraNumero; }
    public static Integer getTessera() { return tessera; }

    public static void setUserId(Long id) { userId = id; }

    public static void setAuthenticatedUsername(String uname) { authenticatedUsername = uname; }

}
