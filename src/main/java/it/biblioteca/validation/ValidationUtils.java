package it.biblioteca.validation;

import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Utility centralizzate per la validazione.
 * Le funzioni aggiungono eventuali messaggi di errore alla lista errors passata dal chiamante.
 */
public final class ValidationUtils {

    /**
     * Nota sicurezza:
     * - Limitiamo la lunghezza massima dell'email per prevenire input patologici (DoS).
     * - La regex utilizza quantificatori possessivi (++) e gruppi atomici (?>...) per evitare backtracking.
     * - La validazione resta volutamente "semplice" e non mira a coprire tutti i casi RFC.
     */
    private static final int MAX_EMAIL_LENGTH = 254;

    // Gruppi atomici + quantificatori possessivi per evitare backtracking: username @ dominio . TLD
    // username:  [A-Za-z0-9._%+-]+
    // dominio:   [A-Za-z0-9.-]+
    // TLD:       [A-Za-z]{2,63}
    private static final Pattern EMAIL = Pattern.compile(
            "^(?>[A-Za-z0-9._%+-]++)@(?>[A-Za-z0-9.-]++)\\.(?>[A-Za-z]{2,63})$"
    );

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

        // Bound di lunghezza per mitigare ReDoS: evita input eccessivi alla regex.
        if (e.length() > MAX_EMAIL_LENGTH) {
            errors.add("Email troppo lunga (max " + MAX_EMAIL_LENGTH + " caratteri).");
            return;
        }

        // Match con regex non backtracking (possessiva/atomica)
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