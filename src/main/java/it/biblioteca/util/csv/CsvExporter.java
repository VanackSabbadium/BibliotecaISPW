package it.biblioteca.util.csv;

import it.biblioteca.entity.Book;
import it.biblioteca.entity.Prestito;
import it.biblioteca.entity.Utente;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

public final class CsvExporter {

    private CsvExporter() {}

    public static void exportBooks(List<Book> books, java.io.File file) throws IOException {
        try (BufferedWriter w = writer(file)) {
            String[] header = { "ID", "ISBN", "Titolo", "Autore", "Pubblicazione", "CasaEditrice", "Copie" };
            writeLine(w, header);
            for (Book b : books) {
                String id = b.getId() != null ? String.valueOf(b.getId()) : "";
                String isbn = nz(b.getIsbn());
                String titolo = nz(b.getTitolo());
                String autore = nz(b.getAutore());
                String pub = b.getDataPubblicazione() != null ? b.getDataPubblicazione().toString() : "";
                String editore = nz(b.getCasaEditrice());
                String copie = String.valueOf(b.getCopie());
                writeLine(w, id, isbn, titolo, autore, pub, editore, copie);
            }
        }
    }

    public static void exportLoans(List<Prestito> loans, java.io.File file) throws IOException {
        try (BufferedWriter w = writer(file)) {
            String[] header = { "ID", "Libro", "Utente", "DataPrestito", "DataRestituzione", "LibroId", "UtenteId" };
            writeLine(w, header);
            for (Prestito p : loans) {
                String id = p.getId() != null ? String.valueOf(p.getId()) : "";
                String libro = nz(p.getLibroTitoloSnapshot());
                String utente = nz(p.getUtente());
                String dp = toStr(p.getDataPrestito());
                String dr = toStr(p.getDataRestituzione());
                String libroId = p.getLibroId() != null ? String.valueOf(p.getLibroId()) : "";
                String utenteId = p.getUtenteId() != null ? String.valueOf(p.getUtenteId()) : "";
                writeLine(w, id, libro, utente, dp, dr, libroId, utenteId);
            }
        }
    }

    public static void exportUsers(List<Utente> users, java.io.File file) throws IOException {
        try (BufferedWriter w = writer(file)) {
            String[] header = { "ID", "Tessera", "Nome", "Cognome", "Email", "Telefono", "DataAttivazione", "DataScadenza" };
            writeLine(w, header);
            for (Utente u : users) {
                String id = u.getId() != null ? String.valueOf(u.getId()) : "";
                String tess = u.getTessera() != null ? String.valueOf(u.getTessera()) : "";
                String nome = nz(u.getNome());
                String cognome = nz(u.getCognome());
                String email = nz(u.getEmail());
                String tel = nz(u.getTelefono());
                String da = toStr(u.getDataAttivazione());
                String ds = toStr(u.getDataScadenza());
                writeLine(w, id, tess, nome, cognome, email, tel, da, ds);
            }
        }
    }

    private static BufferedWriter writer(java.io.File file) throws IOException {
        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
    }

    private static String nz(String s) { return s != null ? s : ""; }

    private static String toStr(LocalDate d) { return d != null ? d.toString() : ""; }

    private static void writeLine(BufferedWriter w, String... values) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) sb.append(';');
            sb.append(escape(values[i]));
        }
        sb.append('\n');
        w.write(sb.toString());
    }

    private static String escape(String s) {
        if (s == null) return "";
        boolean needQuotes = s.contains(";") || s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        String t = s.replace("\"", "\"\"");
        return needQuotes ? "\"" + t + "\"" : t;
    }
}