package it.biblioteca.dao.json;

import it.biblioteca.bean.PrestitoBean;
import it.biblioteca.dao.PrestitoDAO;
import it.biblioteca.entity.Prestito;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementazione su file JSON del PrestitoDAO.
 * Anche qui, la logica comune (I/O, parsing grezzo del JSON) è stata spostata in JsonStorageSupport.
 */
public class JsonPrestitoDAO implements PrestitoDAO {

    private final File storageFile;
    private final List<Prestito> cache = new ArrayList<>();
    private long nextId = 1L;

    public JsonPrestitoDAO(File baseDir) {
        File dir = baseDir != null ? baseDir : new File("data");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        this.storageFile = new File(dir, "prestiti.json");
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

    // ======================================================
    //                    SUPPORTO INTERNO
    // ======================================================

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

        String json = JsonStorageSupport.readWholeFile(storageFile).trim();
        for (String obj : JsonStorageSupport.splitTopLevelObjects(json)) {
            try {
                Prestito p = parsePrestito(obj);
                if (p.getId() != null && p.getId() >= nextId) {
                    nextId = p.getId() + 1;
                }
                cache.add(p);
            } catch (Exception ignored) {
                // un record rotto non butta giù tutto
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

        JsonStorageSupport.writeWholeFile(storageFile, out.toString());
    }

    // ======================================================
    //              SERIALIZZAZIONE / DESERIALIZZAZIONE
    // ======================================================

    private static String prestitoToJson(Prestito p) {
        return '{'
                + "\"id\":" + (p.getId() == null ? "null" : p.getId())
                + ",\"libroId\":" + (p.getLibroId() == null ? "null" : p.getLibroId())
                + ",\"utenteId\":" + (p.getUtenteId() == null ? "null" : p.getUtenteId())
                + ",\"utente\":" + JsonStorageSupport.quote(p.getUtente())
                + ",\"dataPrestito\":" + JsonStorageSupport.quote(JsonStorageSupport.formatDate(p.getDataPrestito()))
                + ",\"dataRestituzione\":" + JsonStorageSupport.quote(JsonStorageSupport.formatDate(p.getDataRestituzione()))
                + ",\"libroTitoloSnapshot\":" + JsonStorageSupport.quote(p.getLibroTitoloSnapshot())
                + '}';
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

    private static String getStringField(String obj, String field) {
        return JsonStorageSupport.extractRawValue(obj, field);
    }

    private static Long getLongField(String obj, String field) {
        String raw = JsonStorageSupport.extractRawValue(obj, field);
        if (raw == null || raw.isEmpty() || "null".equals(raw)) return null;
        return Long.valueOf(raw);
    }

    private static LocalDate getDateField(String obj, String field) {
        String raw = JsonStorageSupport.extractRawValue(obj, field);
        return JsonStorageSupport.parseDate(raw);
    }
}