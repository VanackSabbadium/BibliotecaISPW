package it.biblioteca.security;

public final class SessionContext {

    public enum AppRole { BIBLIOTECARIO, UTENTE }

    private static volatile AppRole currentRole = null;
    private static volatile Integer tessera = null;

    private SessionContext() {}

    public static void setRole(AppRole role) {
        currentRole = role;
    }

    public static AppRole getRole() {
        return currentRole;
    }

    public static void setTessera(Integer tesseraNumero) {
        tessera = tesseraNumero;
    }

    public static Integer getTessera() {
        return tessera;
    }

    public static boolean isBibliotecario() {
        return currentRole == AppRole.BIBLIOTECARIO;
    }

    public static boolean isUtente() {
        return currentRole == AppRole.UTENTE;
    }
}
