package it.biblioteca.ui;

import it.biblioteca.ui.ContentManager.Theme;

public class StartupResult {
    private final String username;
    private final String password;
    private final Theme theme;

    public StartupResult(String username, String password, Theme theme) {
        this.username = username;
        this.password = password;
        this.theme = theme;
    }

    public boolean isValid() {
        return username != null && !username.isBlank()
                && password != null && !password.isBlank()
                && theme != null;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public Theme getTheme() { return theme; }
}
