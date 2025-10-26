package it.biblioteca.util.csv;

import it.biblioteca.bean.BookBean;
import it.biblioteca.bean.UtenteBean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

public final class CsvImporter {

    private CsvImporter() {}

    public static List<BookBean> importBooks(File file) throws IOException {
        List<String[]> rows = readAll(file);
        if (rows.isEmpty()) return Collections.emptyList();

        int headerRowIdx = firstNonEmptyRow(rows);
        if (headerRowIdx < 0) return Collections.emptyList();

        String[] header = rows.get(headerRowIdx);
        Map<String, Integer> map = indexHeader(header);

        List<BookBean> result = new ArrayList<>();
        for (int i = headerRowIdx + 1; i < rows.size(); i++) {
            String[] r = rows.get(i);

            String isbn     = get(r, map, "isbn");
            String titolo   = get(r, map, "titolo");
            String autore   = get(r, map, "autore");
            String pub      = get(r, map, "pubblicazione", "datapubblicazione");
            String editore  = get(r, map, "casaeditrice", "editore");
            String copieStr = get(r, map, "copie");

            if (isEmptyRow(r) || (isBlank(titolo) && isBlank(isbn))) continue;

            BookBean b = new BookBean();
            b.setIsbn(trimOrNull(isbn));
            b.setTitolo(trimOrNull(titolo));
            b.setAutore(trimOrNull(autore));
            b.setCasaEditrice(trimOrNull(editore));
            b.setDataPubblicazione(parseDateOrNull(pub));

            int copie = parseIntOrDefault(copieStr);
            if (copie < 0) copie = 0;
            b.setCopie(copie);

            result.add(b);
        }
        return result;
    }

    public static List<UtenteBean> importUsers(File file) throws IOException {
        List<String[]> rows = readAll(file);
        if (rows.isEmpty()) return Collections.emptyList();

        int headerRowIdx = firstNonEmptyRow(rows);
        if (headerRowIdx < 0) return Collections.emptyList();

        String[] header = rows.get(headerRowIdx);
        Map<String, Integer> map = indexHeader(header);

        List<UtenteBean> result = new ArrayList<>();
        for (int i = headerRowIdx + 1; i < rows.size(); i++) {
            String[] r = rows.get(i);

            String tessStr = get(r, map, "tessera");
            Integer tessera = parseIntOrNull(tessStr);

            String nome  = get(r, map, "nome");
            String cognome = get(r, map, "cognome");
            String email = get(r, map, "email");
            String tel   = get(r, map, "telefono");
            String da    = get(r, map, "dataattivazione", "attivazione");
            String ds    = get(r, map, "datascadenza", "scadenza");

            if (isEmptyRow(r) || tessera == null) continue;

            UtenteBean u = new UtenteBean();
            u.setTessera(tessera);
            u.setNome(trimOrNull(nome));
            u.setCognome(trimOrNull(cognome));
            u.setEmail(trimOrNull(email));
            u.setTelefono(trimOrNull(tel));
            u.setDataAttivazione(parseDateOrNull(da));
            u.setDataScadenza(parseDateOrNull(ds));

            result.add(u);
        }
        return result;
    }

    private static List<String[]> readAll(File file) throws IOException {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                rows.add(parseCsvLine(line));
            }
        }
        return rows;
    }

    private static String[] parseCsvLine(String line) {
        if (line == null) return new String[0];

        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();

        boolean inQuotes = false;
        int i = 0;
        final int len = line.length();

        while (i < len) {
            char ch = line.charAt(i);
            if (inQuotes) {
                int next = consumeQuotedSegment(line, i, cur);
                if (next <= i) {
                    i++;
                } else {
                    inQuotes = isStillInQuotes(line, next);
                    i = next;
                }
            } else {
                i = consumeUnquotedChar(line, i, ch, cur, out);
                inQuotes = becameQuoted(ch);
            }
        }

        addField(out, cur);
        return out.toArray(new String[0]);
    }

    private static int consumeQuotedSegment(String line, int i, StringBuilder cur) {
        final int len = line.length();
        while (i < len) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (i + 1 < len && line.charAt(i + 1) == '"') {
                    cur.append('"');
                    i += 2;
                } else {
                    return i + 1;
                }
            } else {
                cur.append(ch);
                i++;
            }
        }
        return i;
    }

    private static boolean isStillInQuotes(String line, int idxAfterQuote) {
        if (line != null && idxAfterQuote >= 0 && idxAfterQuote <= line.length()) {
            // no-op
        }
        return false;
    }


    private static int consumeUnquotedChar(String line, int i, char ch, StringBuilder cur, List<String> out) {
        if (ch == ';' && i <= line.length()) {
            addField(out, cur);
            return i + 1;
        }
        if (ch == '"' && i < line.length()) {
            return i + 1;
        }
        cur.append(ch);
        return i + 1;
    }

    private static boolean becameQuoted(char ch) {
        return ch == '"';
    }

    private static void addField(List<String> out, StringBuilder cur) {
        out.add(cur.toString());
        cur.setLength(0);
    }

    private static Map<String, Integer> indexHeader(String[] header) {
        Map<String, Integer> map = new HashMap<>();
        if (header == null) return map;
        for (int i = 0; i < header.length; i++) {
            String key = normalize(header[i]);
            if (!key.isEmpty()) map.put(key, i);
        }
        return map;
    }

    private static String get(String[] row, Map<String, Integer> map, String... keys) {
        for (String k : keys) {
            Integer idx = map.get(normalize(k));
            if (idx != null && idx >= 0 && idx < row.length) {
                return row[idx];
            }
        }
        return "";
    }

    private static String normalize(String s) {
        if (s == null) return "";
        return s.toLowerCase(Locale.ITALIAN).replaceAll("[^a-z0-9]", "");
    }

    private static boolean isEmptyRow(String[] r) {
        if (r == null) return true;
        for (String s : r) {
            if (s != null && !s.trim().isEmpty()) return false;
        }
        return true;
    }

    private static int firstNonEmptyRow(List<String[]> rows) {
        for (int i = 0; i < rows.size(); i++) {
            if (!isEmptyRow(rows.get(i))) return i;
        }
        return -1;
    }

    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    private static String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static LocalDate parseDateOrNull(String s) {
        if (isBlank(s)) return null;
        try {
            return LocalDate.parse(s.trim());
        } catch (Exception _) {
            return null;
        }
    }

    private static Integer parseIntOrNull(String s) {
        if (isBlank(s)) return null;
        try {
            return Integer.valueOf(s.trim());
        } catch (Exception _) {
            return null;
        }
    }

    private static Integer parseIntOrDefault(String s) {
        Integer v = parseIntOrNull(s);
        return v != null ? v : 1;
    }
}