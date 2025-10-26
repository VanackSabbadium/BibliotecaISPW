package it.biblioteca.dao.json;

import it.biblioteca.dao.BookDAO;
import it.biblioteca.entity.Book;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JsonBookDAO implements BookDAO {

    private final File storageFile;
    private final List<Book> cache = new ArrayList<>();
    private long nextId = 1L;

    public JsonBookDAO(File baseDir) {
        if (baseDir == null) {
            baseDir = new File("data");
        }
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
        this.storageFile = new File(baseDir, "books.json");
        loadFromDisk();
    }

    @Override
    public synchronized void salvaLibro(Book book) {
        if (book == null) return;

        if (book.getId() == null) {
            book.setId(nextId++);
        }

        Book existing = findByIsbn(book.getIsbn());
        if (existing != null) {
            existing.setTitolo(book.getTitolo());
            existing.setAutore(book.getAutore());
            existing.setDataPubblicazione(book.getDataPubblicazione());
            existing.setCasaEditrice(book.getCasaEditrice());
            existing.setCopie(book.getCopie());
        } else {
            cache.add(cloneBook(book));
        }

        saveToDisk();
    }

    @Override
    public synchronized void aggiornaLibro(Book book) {
        if (book == null || book.getId() == null) return;

        Book target = findById(book.getId());
        if (target != null) {
            target.setIsbn(book.getIsbn());
            target.setTitolo(book.getTitolo());
            target.setAutore(book.getAutore());
            target.setDataPubblicazione(book.getDataPubblicazione());
            target.setCasaEditrice(book.getCasaEditrice());
            target.setCopie(book.getCopie());
            saveToDisk();
        }
    }

    @Override
    public synchronized void eliminaLibro(Long id) {
        if (id == null) return;
        boolean removed = cache.removeIf(b -> id.equals(b.getId()));
        if (removed) {
            saveToDisk();
        }
    }

    @Override
    public synchronized List<Book> trovaTutti() {
        List<Book> result = new ArrayList<>();
        for (Book b : cache) {
            result.add(cloneBook(b));
        }
        return Collections.unmodifiableList(result);
    }

    private Book findById(Long id) {
        for (Book b : cache) {
            if (id.equals(b.getId())) return b;
        }
        return null;
    }

    private Book findByIsbn(String isbn) {
        if (isbn == null) return null;
        for (Book b : cache) {
            if (isbn.equals(b.getIsbn())) return b;
        }
        return null;
    }

    private Book cloneBook(Book src) {
        Book b = new Book();
        b.setId(src.getId());
        b.setIsbn(src.getIsbn());
        b.setTitolo(src.getTitolo());
        b.setAutore(src.getAutore());
        b.setDataPubblicazione(src.getDataPubblicazione());
        b.setCasaEditrice(src.getCasaEditrice());
        b.setCopie(src.getCopie());
        return b;
    }

    private void loadFromDisk() {
        cache.clear();
        nextId = 1L;

        if (!storageFile.exists()) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = Files.newBufferedReader(storageFile.toPath(), StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException ignored) {
            return;
        }

        String json = sb.toString().trim();
        for (String obj : splitTopLevelObjects(json)) {
            try {
                Book b = parseBook(obj);
                if (b.getId() != null && b.getId() >= nextId) {
                    nextId = b.getId() + 1;
                }
                cache.add(b);
            } catch (Exception ignored) {
            }
        }
    }

    private void saveToDisk() {
        StringBuilder out = new StringBuilder();
        out.append('[').append('\n');
        for (int i = 0; i < cache.size(); i++) {
            out.append("  ").append(bookToJson(cache.get(i)));
            if (i < cache.size() - 1) out.append(',');
            out.append('\n');
        }
        out.append(']').append('\n');

        try (BufferedWriter bw = Files.newBufferedWriter(
                storageFile.toPath(),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE)) {
            bw.write(out.toString());
        } catch (IOException ignored) {
        }
    }

    private static String bookToJson(Book b) {
        return '{' +
                "\"id\":" + (b.getId() == null ? "null" : b.getId()) +
                ",\"isbn\":" + quote(b.getIsbn()) +
                ",\"titolo\":" + quote(b.getTitolo()) +
                ",\"autore\":" + quote(b.getAutore()) +
                ",\"dataPubblicazione\":" + quote(formatDate(b.getDataPubblicazione())) +
                ",\"casaEditrice\":" + quote(b.getCasaEditrice()) +
                ",\"copie\":" + b.getCopie() +
                '}';
    }

    private static Book parseBook(String obj) {
        Book b = new Book();

        Long idVal = getLongField(obj);
        if (idVal != null) b.setId(idVal);

        b.setIsbn(getStringField(obj, "isbn"));
        b.setTitolo(getStringField(obj, "titolo"));
        b.setAutore(getStringField(obj, "autore"));
        b.setDataPubblicazione(getDateField(obj));
        b.setCasaEditrice(getStringField(obj, "casaEditrice"));

        Integer copieVal = getIntField(obj);
        if (copieVal != null) b.setCopie(copieVal);

        return b;
    }

    private static String quote(String s) {
        return "\"" + escapeJson(s == null ? "" : s) + "\"";
    }

    private static String escapeJson(String s) {
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

    private static String formatDate(LocalDate d) {
        return (d == null) ? "" : d.toString();
    }

    private static LocalDate parseDate(String v) {
        if (v == null || v.isEmpty()) return null;
        return LocalDate.parse(v);
    }

    private static List<String> splitTopLevelObjects(String jsonArray) {
        List<String> objs = new ArrayList<>();
        if (jsonArray == null || jsonArray.isEmpty()) return objs;
        String trimmed = jsonArray.trim();
        if (trimmed.isEmpty() || trimmed.equals("[]")) return objs;

        if (trimmed.charAt(0) == '[') trimmed = trimmed.substring(1);
        if (trimmed.endsWith("]")) trimmed = trimmed.substring(0, trimmed.length() - 1);

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
                } else if (ch == '\"') {
                    inStr = false;
                }
            } else {
                if (ch == '\"') {
                    inStr = true;
                } else if (ch == '{') {
                    if (depth == 0) start = i;
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

    private static String extractRawValue(String obj, String field) {
        String key = "\"" + field + "\"";
        int idx = obj.indexOf(key);
        if (idx < 0) return null;
        int colon = obj.indexOf(':', idx + key.length());
        if (colon < 0) return null;
        int i = colon + 1;
        final int len = obj.length();
        while (i < len && Character.isWhitespace(obj.charAt(i))) i++;
        if (i >= len) return null;

        char first = obj.charAt(i);
        if (first == '\"') {
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
                } else if (c == '\"') {
                    break;
                } else {
                    sb.append(c);
                }
                i++;
            }
            return sb.toString();
        } else {
            int j = i;
            while (j < len) {
                char c = obj.charAt(j);
                if (c == ',' || c == '}') break;
                j++;
            }
            return obj.substring(i, j).trim();
        }
    }

    private static String getStringField(String obj, String field) {
        return extractRawValue(obj, field);
    }

    private static Integer getIntField(String obj) {
        String raw = extractRawValue(obj, "copie");
        if (raw == null || raw.isEmpty() || "null".equals(raw)) return null;
        return Integer.valueOf(raw);
    }

    private static Long getLongField(String obj) {
        String raw = extractRawValue(obj, "id");
        if (raw == null || raw.isEmpty() || "null".equals(raw)) return null;
        return Long.valueOf(raw);
    }

    private static LocalDate getDateField(String obj) {
        String raw = extractRawValue(obj, "dataPubblicazione");
        return parseDate(raw);
    }
}