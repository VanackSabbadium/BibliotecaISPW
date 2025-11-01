package it.biblioteca.security;

import it.biblioteca.dao.DaoFactory;

public final class SessionContext {

    private SessionContext() {}

    public enum AppRole {
        ADMIN, BIBLIOTECARIO, UTENTE;

        public static AppRole fromDbRole(String s) {
            if (s == null) return null;
            try {
                return AppRole.valueOf(s.trim().toUpperCase());
            } catch (Exception _) {
                return null;
            }
        }
    }

    private static DaoFactory daoFactory;

    private static String role;
    private static Integer tessera;

    public static void setDaoFactory(DaoFactory f) { daoFactory = f; }
    public static DaoFactory getDaoFactory() { return daoFactory; }

    public static void setRole(String r) { role = r; }

    public static boolean isAdmin() { return "ADMIN".equalsIgnoreCase(role); }
    public static boolean isBibliotecario() { return "BIBLIOTECARIO".equalsIgnoreCase(role); }
    public static boolean isUtente() { return "UTENTE".equalsIgnoreCase(role); }

    public static void setUserId() {
        // empty
    }

    public static void setTessera(Integer t) { tessera = t; }
    public static Integer getTessera() { return tessera; }

}
