package it.biblioteca.dao;

import it.biblioteca.entity.AppUser;

public interface AppUserDAO {
    AppUser findByUsername(String username);
    AppUser findByUtenteId(Long utenteId);
    boolean insertAppUser(AppUser u);
    boolean usernameExists(String username);
}