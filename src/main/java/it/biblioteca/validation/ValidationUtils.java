package it.biblioteca.validation;

import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

public final class ValidationUtils {

    private static final int MAX_EMAIL_LENGTH = 254;

    private static final Pattern EMAIL = Pattern.compile(
            "^(?>[A-Za-z0-9._%+-]++)@(?>[A-Za-z0-9.-]++)\\.(?>[A-Za-z]{2,63})$"
    );

    private ValidationUtils() {}

    public static Integer parseInteger(String value, String fieldName, boolean required, List<String> errors) {
        String s = value == null ? "" : value.trim();
        if (s.isEmpty()) {
            if (required) errors.add(fieldName + " è obbligatorio.");
            return null;
        }
        try {
            return Integer.valueOf(s);
        } catch (NumberFormatException _) {
            errors.add(fieldName + " deve essere numerico.");
            return null;
        }
    }

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

    public static void validateDateOrder(LocalDate start, LocalDate end, String startName, String endName, List<String> errors) {
        if (start != null && end != null && end.isBefore(start)) {
            errors.add(endName + " non può essere precedente a " + startName + ".");
        }
    }

    public static void requireNotNull(Object obj, String fieldName, List<String> errors) {
        if (obj == null) {
            errors.add(fieldName + " è obbligatoria.");
        }
    }
}