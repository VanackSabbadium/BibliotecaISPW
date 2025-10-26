package it.biblioteca.ui;

public class StartupResult {
    private final String dbUsername;
    private final String dbPassword;
    private final String appUsername;
    private final String appPassword;
    private final ContentManager.Theme theme;

    public StartupResult(String dbUsername, String dbPassword,
                         String appUsername, String appPassword,
                         ContentManager.Theme theme) {
        this.dbUsername = dbUsername;
        this.dbPassword = dbPassword;
        this.appUsername = appUsername;
        this.appPassword = appPassword;
        this.theme = theme;
    }

    public String getUsername() { return dbUsername; }
    public String getPassword() { return dbPassword; }

    public String getAppUsername() { return appUsername; }
    public String getAppPassword() { return appPassword; }

    public ContentManager.Theme getTheme() { return theme; }

    public boolean isValid() {
        return dbUsername != null && !dbUsername.trim().isEmpty()
                && dbPassword != null && !dbPassword.trim().isEmpty()
                && appUsername != null && !appUsername.trim().isEmpty()
                && appPassword != null && !appPassword.trim().isEmpty()
                && theme != null;
    }
}