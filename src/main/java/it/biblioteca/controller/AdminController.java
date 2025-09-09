package it.biblioteca.controller;

import it.biblioteca.dao.AppUserDAO;
import it.biblioteca.dao.AppUserDAOImpl;
import it.biblioteca.entity.AppUser;
import it.biblioteca.util.PasswordUtils;

public class AdminController {
    private final AppUserDAO appUserDAO;

    public AdminController() {
        this(new AppUserDAOImpl());
    }

    public AdminController(AppUserDAO dao) {
        this.appUserDAO = dao;
    }

    /**
     * Crea credenziali applicative per un utente (associa username/password all'utente esistente).
     * Restituisce true se create con successo.
     */
    public boolean creaCredenzialiPerUtente(Long utenteId, String username, String plainPassword, String createdBy) {
        if (utenteId == null || username == null || username.isBlank() || plainPassword == null || plainPassword.isBlank()) {
            return false;
        }
        if (appUserDAO.usernameExists(username)) return false;
        AppUser u = new AppUser();
        u.setUtenteId(utenteId);
        u.setUsername(username);
        u.setPasswordHash(PasswordUtils.sha256Hex(plainPassword));
        u.setRole("UTENTE");
        u.setCreatedBy(createdBy);
        return appUserDAO.insertAppUser(u);
    }
}
