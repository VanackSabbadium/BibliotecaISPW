package it.biblioteca.dao.json;

import it.biblioteca.bean.PrestitoBean;
import it.biblioteca.dao.PrestitoDAO;
import it.biblioteca.entity.Prestito;

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

public class JsonPrestitoDAO implements PrestitoDAO {

    private final File storageFile;
    private final List<Prestito> cache = new ArrayList<>();
    private long nextId = 1L;

    public JsonPrestitoDAO(File baseDir) {
        if (baseDir == null) {
            baseDir = new File("data");
        }
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
        this.storageFile = new File(baseDir, "prestiti.json");
        loadFromDisk();
    }

    @Override
    public synchronized List<Prestito> trovaTutti() {
        List<Prestito> result = new ArrayList<>();
        for (Prestito p : cache) {
            result.add(clonePrestito(p));
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public synchronized List<Prestito> trovaPrestitiAttivi() {
        List<Prestito> result = new ArrayList<>();
        for (Prestito p : cache) {
            if (p.getDataRestituzione() == null) {
                result.add(clonePrestito(p));
            }
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public synchronized boolean inserisci(PrestitoBean bean) {
        if (bean == null) return false;

        Prestito p = new Prestito();
        p.setId(nextId++);
        p.setLibroId(bean.getLibroId());
        p.setUtenteId(bean.getUtenteId());
        p.setUtente(bean.getUtenteSnapshot());
        p.setLibroTitoloSnapshot(bean.getLibroTitoloSnapshot());

        LocalDate dp = bean.getDataPrestito() != null ? bean.getDataPrestito() : LocalDate.now();
        p.setDataPrestito(dp);
        p.setDataRestituzione(null);

        cache.add(p);
        saveToDisk();
        return true;
    }

    @Override
    public synchronized boolean chiudiPrestito(Long prestitoId, LocalDate dataRestituzione) {
        if (prestitoId == null) return false;
        Prestito p = findById(prestitoId);
        if (p == null) return false;
        if (p.getDataRestituzione() != null) return false;

        p.setDataRestituzione(dataRestituzione != null ? dataRestituzione : LocalDate.now());
        saveToDisk();
        return true;
    }

    private Prestito findById(Long id) {
        for (Prestito p : cache) {
            if (id.equals(p.getId())) return p;
        }
        return null;
    }

    private Prestito clonePrestito(Prestito src) {
        Prestito p = new Prestito();
        p.setId(src.getId());
        p.setLibroId(src.getLibroId());
        p.setUtenteId(src.getUtenteId());
        p.setUtente(src.getUtente());
        p.setDataPrestito(src.getDataPrestito());
        p.setDataRestituzione(src.getDataRestituzione());
        p.setLibroTitoloSnapshot(src.getLibroTitoloSnapshot());
        return p;
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
                Prestito p = parsePrestito(obj);
                if (p.getId() != null && p.getId() >= nextId) {
                    nextId = p.getId() + 1;
                }
                cache.add(p);
            } catch (Exception ignored) {
            }
        }
    }

    private void saveToDisk() {
        StringBuilder out = new StringBuilder();
        out.append('[').append('\n');
        for (int i = 0; i < cache.size(); i++) {
            out.append("  ").append(prestitoToJson(cache.get(i)));
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

    private static String prestitoToJson(Prestito p) {
        return '{' +
                "\"id\":" + (p.getId() == null ? "null" : p.getId()) +
                ",\"libroId\":" + (p.getLibroId() == null ? "null" : p.getLibroId()) +
                ",\"utenteId\":" + (p.getUtenteId() == null ? "null" : p.getUtenteId()) +
                ",\"utente\":" + quote(p.getUtente()) +
                ",\"dataPrestito\":" + quote(formatDate(p.getDataPrestito())) +
                ",\"dataRestituzione\":" + quote(formatDate(p.getDataRestituzione())) +
                ",\"libroTitoloSnapshot\":" + quote(p.getLibroTitoloSnapshot()) +
                '}';
    }

    private static Prestito parsePrestito(String obj) {
        Prestito p = new Prestito();
        Long idVal = getLongField(obj, "id");
        if (idVal != null) p.setId(idVal);

        Long libroId = getLongField(obj, "libroId");
        if (libroId != null) p.setLibroId(libroId);

        Long utenteId = getLongField(obj, "utenteId");
        if (utenteId != null) p.setUtenteId(utenteId);

        p.setUtente(getStringField(obj, "utente"));
        p.setDataPrestito(getDateField(obj, "dataPrestito"));
        p.setDataRestituzione(getDateField(obj, "dataRestituzione"));
        p.setLibroTitoloSnapshot(getStringField(obj, "libroTitoloSnapshot"));

        return p;
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

    private static Long getLongField(String obj, String field) {
        String raw = extractRawValue(obj, field);
        if (raw == null || raw.isEmpty() || "null".equals(raw)) return null;
        return Long.valueOf(raw);
    }

    private static LocalDate getDateField(String obj, String field) {
        String raw = extractRawValue(obj, field);
        return parseDate(raw);
    }
}