package it.biblioteca.dao.json;

import it.biblioteca.dao.BookDAO;
import it.biblioteca.entity.Book;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementazione su file JSON del BookDAO.
 * Ora usa JsonStorageSupport per ridurre duplicazioni.
 */
public class JsonBookDAO implements BookDAO {

    private final File storageFile;
    private final List<Book> cache = new ArrayList<>();
    private long nextId = 1L;

    public JsonBookDAO(File baseDir) {
        File dir = baseDir != null ? baseDir : new File("data");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        this.storageFile = new File(dir, "books.json");
        loadFromDisk();
    }

    @Override
    public synchronized void salvaLibro(Book book) {
        if (book == null) return;

        // assegna ID se nuovo
        if (book.getId() == null) {
            book.setId(nextId++);
        }

        // se ISBN già esiste, aggiorna quello
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

    // ======================================================
    //                    SUPPORTO INTERNO
    // ======================================================

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

        String json = JsonStorageSupport.readWholeFile(storageFile).trim();
        for (String obj : JsonStorageSupport.splitTopLevelObjects(json)) {
            try {
                Book b = parseBook(obj);
                if (b.getId() != null && b.getId() >= nextId) {
                    nextId = b.getId() + 1;
                }
                cache.add(b);
            } catch (Exception ignored) {
                // se un record è marcio non blocchiamo tutto
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

        JsonStorageSupport.writeWholeFile(storageFile, out.toString());
    }

    // ======================================================
    //              SERIALIZZAZIONE / DESERIALIZZAZIONE
    // ======================================================

    private static String bookToJson(Book b) {
        return '{'
                + "\"id\":" + (b.getId() == null ? "null" : b.getId())
                + ",\"isbn\":" + JsonStorageSupport.quote(b.getIsbn())
                + ",\"titolo\":" + JsonStorageSupport.quote(b.getTitolo())
                + ",\"autore\":" + JsonStorageSupport.quote(b.getAutore())
                + ",\"dataPubblicazione\":" + JsonStorageSupport.quote(JsonStorageSupport.formatDate(b.getDataPubblicazione()))
                + ",\"casaEditrice\":" + JsonStorageSupport.quote(b.getCasaEditrice())
                + ",\"copie\":" + b.getCopie()
                + '}';
    }

    private static Book parseBook(String obj) {
        Book b = new Book();

        Long idVal = getLongField(obj, "id");
        if (idVal != null) b.setId(idVal);

        b.setIsbn(getStringField(obj, "isbn"));
        b.setTitolo(getStringField(obj, "titolo"));
        b.setAutore(getStringField(obj, "autore"));
        b.setDataPubblicazione(getDateField(obj, "dataPubblicazione"));
        b.setCasaEditrice(getStringField(obj, "casaEditrice"));

        Integer copieVal = getIntField(obj, "copie");
        if (copieVal != null) b.setCopie(copieVal);

        return b;
    }

    private static String getStringField(String obj, String field) {
        return JsonStorageSupport.extractRawValue(obj, field);
    }

    private static Integer getIntField(String obj, String field) {
        String raw = JsonStorageSupport.extractRawValue(obj, field);
        if (raw == null || raw.isEmpty() || "null".equals(raw)) return null;
        return Integer.valueOf(raw);
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