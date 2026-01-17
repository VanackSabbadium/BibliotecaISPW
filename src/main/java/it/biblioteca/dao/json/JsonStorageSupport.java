package it.biblioteca.dao.json;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

final class JsonStorageSupport {

    private JsonStorageSupport() {
        // utility class, niente istanze
    }

    static String readWholeFile(File file) {
        try {
            return Files.readString(file.toPath(), StandardCharsets.UTF_8);
        } catch (IOException _) {
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
        } catch (IOException _) {
            // empty
        }
    }

    static List<String> splitTopLevelObjects(String jsonArray) {
        List<String> objs = new ArrayList<>();
        if (jsonArray == null) return objs;

        String trimmed = jsonArray.trim();
        if (trimmed.length() < 2 || "[]".equals(trimmed)) return objs;

        if (trimmed.charAt(0) == '[' && trimmed.charAt(trimmed.length() - 1) == ']') {
            trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
        }

        final int len = trimmed.length();
        int i = 0;
        while (i < len) {
            char ch = trimmed.charAt(i);
            if (ch == '{') {
                int end = findObjectEnd(trimmed, i);
                if (end < 0) break;
                objs.add(trimmed.substring(i, end + 1).trim());
                i = end + 1;
            } else {
                i++;
            }
        }
        return objs;
    }

    static String extractRawValue(String obj, String field) {
        if (obj == null || field == null) return null;

        String key = "\"" + field + "\"";
        int k = obj.indexOf(key);
        if (k < 0) return null;

        int afterKey = k + key.length();
        int colon = findNextColon(obj, afterKey);
        if (colon < 0) return null;

        int vStart = skipWs(obj, colon + 1);
        if (vStart >= obj.length()) return null;

        char first = obj.charAt(vStart);
        if (first == '"') {
            Read r = readJsonString(obj, vStart);
            return r.value;
        }

        int vEnd = findBareValueEnd(obj, vStart);
        return obj.substring(vStart, vEnd).trim();
    }

    private static int findObjectEnd(CharSequence s, int startBrace) {
        int depth = 1;
        boolean inStr = false;
        boolean esc = false;
        for (int i = startBrace + 1; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (inStr) {
                if (esc) {
                    esc = false;
                } else if (ch == '\\') {
                    esc = true;
                } else if (ch == '\"') {
                    inStr = false;
                }
                continue;
            }
            if (ch == '\"') {
                inStr = true;
            } else if (ch == '{') {
                depth++;
            } else if (ch == '}') {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }

    private static int skipWs(CharSequence s, int i) {
        int len = s.length();
        while (i < len && Character.isWhitespace(s.charAt(i))) i++;
        return i;
    }

    private static int findNextColon(CharSequence s, int from) {
        for (int i = from; i < s.length(); i++) {
            if (s.charAt(i) == ':') return i;
        }
        return -1;
    }

    private static int findBareValueEnd(CharSequence s, int from) {
        for (int i = from; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == ',' || ch == '}') return i;
        }
        return s.length();
    }

    private static Read readJsonString(CharSequence s, int quotePos) {
        StringBuilder sb = new StringBuilder(32);
        boolean esc = false;
        int i = quotePos + 1;
        for (; i < s.length(); i++) {
            char c = s.charAt(i);
            if (esc) {
                sb.append(c);
                esc = false;
            } else if (c == '\\') {
                esc = true;
            } else if (c == '\"') {
                break;
            } else {
                sb.append(c);
            }
        }
        return new Read(sb.toString(), i);
    }

    private static final class Read {
        final String value;
        final int endPos;
        Read(String v, int e) { this.value = v; this.endPos = e; }
    }

    static String quote(String s) {
        return "\"" + escapeJson(s == null ? "" : s) + "\"";
    }

    static String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' || c == '\"') sb.append('\\');
            sb.append(c);
        }
        return sb.toString();
    }

    static String formatDate(LocalDate d) {
        return (d == null) ? "" : d.toString();
    }

    static LocalDate parseDate(String v) {
        if (v == null || v.isBlank() || "null".equalsIgnoreCase(v)) return null;
        return LocalDate.parse(v.trim());
    }
}