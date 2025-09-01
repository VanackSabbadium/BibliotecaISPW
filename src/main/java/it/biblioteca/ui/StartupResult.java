package it.biblioteca.ui;

import it.biblioteca.ui.ContentManager.Theme;

public class StartupResult {
    private final String username;
    private final String password;
    private final Theme theme;
    private final Integer tessera; // richiesto se username == "Utente"

    public StartupResult(String username, String password, Theme theme, Integer tessera) {
        this.username = username;
        this.password = password;
        this.theme = theme;
        this.tessera = tessera;
    }

    public boolean isValid() {
        return username != null && !username.isBlank()
                && password != null && !password.isBlank()
                && theme != null
                && (!"Utente".equalsIgnoreCase(username) || tessera != null);
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public Theme getTheme() { return theme; }
    public Integer getTessera() { return tessera; }
}