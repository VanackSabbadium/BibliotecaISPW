package it.biblioteca.validation;

import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Utility centralizzate per la validazione.
 * Le funzioni aggiungono eventuali messaggi di errore alla lista errors passata dal chiamante.
 */
public final class ValidationUtils {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private ValidationUtils() {}

    /** Aggiunge un errore se la stringa è null o blank. */
    public static void requireNonBlank(String value, String fieldName, List<String> errors) {
        if (value == null || value.isBlank()) {
            errors.add(fieldName + " è obbligatorio.");
        }
    }

    /**
     * Effettua il parse Integer di una stringa.
     * - Se required ed è vuota -> errore
     * - Se non numerica -> errore
     * Ritorna il valore parsato o null (in caso di errore).
     */
    public static Integer parseInteger(String value, String fieldName, boolean required, List<String> errors) {
        String s = value == null ? "" : value.trim();
        if (s.isEmpty()) {
            if (required) errors.add(fieldName + " è obbligatorio.");
            return null;
        }
        try {
            return Integer.valueOf(s);
        } catch (NumberFormatException ex) {
            errors.add(fieldName + " deve essere numerico.");
            return null;
        }
    }

    /** Valida il formato email solo se presente (non obbligatoria). */
    public static void validateEmailIfPresent(String email, List<String> errors) {
        if (email == null || email.isBlank()) return;
        String e = email.trim();
        if (!EMAIL.matcher(e).matches()) {
            errors.add("Email non valida.");
        }
    }

    /**
     * Verifica che end non sia prima di start. Se entrambe non null e l'ordine è sbagliato, aggiunge errore.
     */
    public static void validateDateOrder(LocalDate start, LocalDate end, String startName, String endName, List<String> errors) {
        if (start != null && end != null && end.isBefore(start)) {
            errors.add(endName + " non può essere precedente a " + startName + ".");
        }
    }

    /** Aggiunge un errore se l'oggetto è null. */
    public static void requireNotNull(Object obj, String fieldName, List<String> errors) {
        if (obj == null) {
            errors.add(fieldName + " è obbligatoria.");
        }
    }
}