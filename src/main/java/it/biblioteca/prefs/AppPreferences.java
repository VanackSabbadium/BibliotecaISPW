package it.biblioteca.prefs;

import javafx.application.Platform;
import javafx.stage.Stage;

import java.io.File;
import java.util.prefs.Preferences;

/**
 * Gestione preferenze applicative usando Java Preferences API.
 * Salva/Ripristina:
 *  - Tema scelto (COLORI/BIANCO_NERO)
 *  - Ultima tab selezionata (home/catalogo/prestiti/utenti/profilo/miei_prestiti)
 *  - Ultima directory usata per CSV
 *  - Geometria finestra (x, y, width, height) – opzionale, via applyStageGeometry
 */
public final class AppPreferences {

    private static final String NODE = "it/biblioteca/app";
    private static final String KEY_THEME = "theme"; // "COLORI" | "BIANCO_NERO"
    private static final String KEY_LAST_TAB = "lastTab"; // "home" | "catalogo" | "prestiti" | "utenti" | "profilo" | "miei_prestiti"
    private static final String KEY_LAST_DIR = "lastDir";

    // stage geometry
    private static final String KEY_STAGE_X = "stageX";
    private static final String KEY_STAGE_Y = "stageY";
    private static final String KEY_STAGE_W = "stageW";
    private static final String KEY_STAGE_H = "stageH";

    private AppPreferences() {
    }

    private static Preferences prefs() {
        return Preferences.userRoot().node(NODE);
    }

    // ===== Theme =====
    public static String loadThemeOrDefault() {
        return prefs().get(KEY_THEME, "COLORI");
    }

    public static void saveTheme(String themeName) {
        try {
            if (themeName != null) prefs().put(KEY_THEME, themeName);
        } catch (SecurityException ignored) {
        }
    }

    // ===== Last tab =====
    public static String loadLastTabOrDefault() {
        return prefs().get(KEY_LAST_TAB, "home");
    }

    public static void saveLastTab(String tabId) {
        try {
            if (tabId != null) prefs().put(KEY_LAST_TAB, tabId);
        } catch (SecurityException ignored) {
        }
    }

    // ===== Last CSV dir =====
    public static File loadLastDirectoryOrNull() {
        String path = prefs().get(KEY_LAST_DIR, null);
        if (path == null || path.isBlank()) return null;
        File f = new File(path);
        return f.exists() && f.isDirectory() ? f : null;
    }

    public static void saveLastDirectory(File dir) {
        try {
            if (dir != null && dir.exists() && dir.isDirectory()) {
                prefs().put(KEY_LAST_DIR, dir.getAbsolutePath());
            }
        } catch (SecurityException ignored) {
        }
    }

    // ===== Stage geometry (opzionale) =====
    public static void applyStageGeometry(Stage stage) {
        if (stage == null) return;
        try {
            double x = prefs().getDouble(KEY_STAGE_X, Double.NaN);
            double y = prefs().getDouble(KEY_STAGE_Y, Double.NaN);
            double w = prefs().getDouble(KEY_STAGE_W, Double.NaN);
            double h = prefs().getDouble(KEY_STAGE_H, Double.NaN);

            if (!Double.isNaN(w) && !Double.isNaN(h)) {
                stage.setWidth(w);
                stage.setHeight(h);
            }
            if (!Double.isNaN(x) && !Double.isNaN(y)) {
                stage.setX(x);
                stage.setY(y);
            }

            // Salva in tempo reale
            stage.xProperty().addListener((obs, o, nv) -> saveStageX(stage.getX()));
            stage.yProperty().addListener((obs, o, nv) -> saveStageY(stage.getY()));
            stage.widthProperty().addListener((obs, o, nv) -> saveStageW(stage.getWidth()));
            stage.heightProperty().addListener((obs, o, nv) -> saveStageH(stage.getHeight()));

            stage.setOnCloseRequest(evt -> {
                saveStageX(stage.getX());
                saveStageY(stage.getY());
                saveStageW(stage.getWidth());
                saveStageH(stage.getHeight());
                Platform.exit();
            });
        } catch (SecurityException ignored) {
        }
    }

    private static void saveStageX(double x) {
        try {
            prefs().putDouble(KEY_STAGE_X, x);
        } catch (SecurityException ignored) {
        }
    }

    private static void saveStageY(double y) {
        try {
            prefs().putDouble(KEY_STAGE_Y, y);
        } catch (SecurityException ignored) {
        }
    }

    private static void saveStageW(double w) {
        try {
            prefs().putDouble(KEY_STAGE_W, w);
        } catch (SecurityException ignored) {
        }
    }

    private static void saveStageH(double h) {
        try {
            prefs().putDouble(KEY_STAGE_H, h);
        } catch (SecurityException ignored) {
        }
    }
}