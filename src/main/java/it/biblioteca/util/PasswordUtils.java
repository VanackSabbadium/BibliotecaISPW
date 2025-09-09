package it.biblioteca.util;

import java.security.MessageDigest;

public final class PasswordUtils {
    private PasswordUtils() {}

    public static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(input.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Errore hashing password", e);
        }
    }

    public static boolean verify(String plain, String hashHex) {
        if (plain == null || hashHex == null) return false;
        return sha256Hex(plain).equalsIgnoreCase(hashHex);
    }
}