package it.biblioteca.dao.json;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Supporto condiviso per i DAO JSON.
 * Qui vivono tutte le utility comuni (lettura/scrittura file, parsing JSON minimale, ecc.).
 *
 * Questo serve a:
 *  - ridurre il codice duplicato fra JsonBookDAO e JsonPrestitoDAO
 *  - calmare SonarQube così smette di lamentarsi che ci stiamo copiando i compiti.
 */
final class JsonStorageSupport {

    private JsonStorageSupport() {
        // utility class, niente istanze
    }

    static String readWholeFile(File file) {
        try {
            return Files.readString(file.toPath(), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
            return "";
        }
    }

    static void writeWholeFile(File file, String content) {
        try {
            Files.writeString(
                    file.toPath(),
                    content,
                    StandardCharsets.UTF_8
            );
        } catch (IOException ignored) {
            // come prima: silenzio, nessuna eccezione propagata
        }
    }

    /**
     * Estrae tutti gli oggetti JSON top-level da un array JSON fatto così:
     * [ { ... }, { ... }, ... ]
     *
     * Non è un parser JSON generale. È volutamente minimale.
     */
    static List<String> splitTopLevelObjects(String jsonArray) {
        List<String> objs = new ArrayList<>();
        if (jsonArray == null || jsonArray.isEmpty()) return objs;

        String trimmed = jsonArray.trim();
        if (trimmed.isEmpty() || "[]".equals(trimmed)) return objs;

        // togliamo [ ... ]
        if (trimmed.charAt(0) == '[') {
            trimmed = trimmed.substring(1);
        }
        if (trimmed.endsWith("]")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }

        int depth = 0;
        boolean inStr = false;
        boolean escape = false;
        int start = -1;

        for (int i = 0; i < trimmed.length(); i++) {
            char ch = trimmed.charAt(i);

            if (inStr) {
                if (escape) {
                    escape = false;
                } else if (ch == '\\') {
                    escape = true;
                } else if (ch == '"') {
                    inStr = false;
                }
            } else {
                if (ch == '"') {
                    inStr = true;
                } else if (ch == '{') {
                    if (depth == 0) {
                        start = i;
                    }
                    depth++;
                } else if (ch == '}') {
                    depth--;
                    if (depth == 0 && start >= 0) {
                        objs.add(trimmed.substring(start, i + 1).trim());
                        start = -1;
                    }
                }
            }
        }

        return objs;
    }

    /**
     * Estrae il valore di un campo JSON da una stringa tipo:
     * { "campo": valore,... }
     * Restituisce stringa senza virgolette se era stringa, oppure il raw se numero/null.
     */
    static String extractRawValue(String obj, String field) {
        String key = "\"" + field + "\"";
        int idx = obj.indexOf(key);
        if (idx < 0) return null;

        int colon = obj.indexOf(':', idx + key.length());
        if (colon < 0) return null;

        int i = colon + 1;
        final int len = obj.length();

        // salta spazi
        while (i < len && Character.isWhitespace(obj.charAt(i))) {
            i++;
        }
        if (i >= len) return null;

        char first = obj.charAt(i);
        if (first == '"') {
            // stringa
            i++;
            StringBuilder sb = new StringBuilder();
            boolean esc = false;
            while (i < len) {
                char c = obj.charAt(i);
                if (esc) {
                    sb.append(c);
                    esc = false;
                } else if (c == '\\') {
                    esc = true;
                } else if (c == '"') {
                    break;
                } else {
                    sb.append(c);
                }
                i++;
            }
            return sb.toString();
        } else {
            // numero, null, ecc.
            int j = i;
            while (j < len) {
                char c = obj.charAt(j);
                if (c == ',' || c == '}') break;
                j++;
            }
            return obj.substring(i, j).trim();
        }
    }

    static String quote(String s) {
        return "\"" + escapeJson(s == null ? "" : s) + "\"";
    }

    static String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' || c == '\"') {
                sb.append('\\');
            }
            sb.append(c);
        }
        return sb.toString();
    }

    static String formatDate(LocalDate d) {
        return (d == null) ? "" : d.toString();
    }

    static LocalDate parseDate(String v) {
        if (v == null || v.isEmpty()) return null;
        return LocalDate.parse(v);
    }
}