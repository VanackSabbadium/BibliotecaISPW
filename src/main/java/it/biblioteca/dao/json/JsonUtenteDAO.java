package it.biblioteca.dao.json;

import it.biblioteca.dao.UtenteDAO;
import it.biblioteca.entity.Utente;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JsonUtenteDAO implements UtenteDAO {

    private static final Logger LOGGER = Logger.getLogger(JsonUtenteDAO.class.getName());

    private final File usersFile;

    private final List<Utente> utenti = new ArrayList<>();
    private final List<CredRow> credenziali = new ArrayList<>();

    private long userSeq = 0L;

    private static final class CredRow {
        Long userId;
        String username;
        String passwordHash; // SHA-256 hex
        String role;         // "ADMIN" | "BIBLIOTECARIO" | "UTENTE"

        CredRow(Long userId, String username, String passwordHash, String role) {
            this.userId = userId;
            this.username = username;
            this.passwordHash = passwordHash;
            this.role = role;
        }
    }

    public JsonUtenteDAO(File baseDir) {
        this.usersFile = new File(baseDir, "utenti.json");
        loadFromFile();
    }

    @Override
    public synchronized List<Utente> trovaTutti() {
        List<Utente> copy = new ArrayList<>();
        for (Utente u : utenti) {
            copy.add(cloneUtente(u));
        }
        copy.sort(
                Comparator.comparing(Utente::getCognome, nullSafeString())
                        .thenComparing(Utente::getNome, nullSafeString())
        );
        return copy;
    }

    @Override
    public synchronized Utente trovaPerId(Long id) {
        if (id == null) return null;
        for (Utente u : utenti) {
            if (id.equals(u.getId())) {
                return cloneUtente(u);
            }
        }
        return null;
    }

    @Override
    public synchronized boolean aggiungi(Utente u) {
        if (u == null) return false;
        if (u.getId() == null) {
            userSeq++;
            u.setId(userSeq);
        } else {
            if (findIndexById(u.getId()) != -1) {
                return false; // già esiste
            }
            if (u.getId() > userSeq) {
                userSeq = u.getId();
            }
        }
        utenti.add(cloneUtente(u));
        saveToFile();
        return true;
    }

    @Override
    public synchronized boolean aggiorna(Utente u) {
        if (u == null || u.getId() == null) return false;
        int idx = findIndexById(u.getId());
        if (idx == -1) return false;
        utenti.set(idx, cloneUtente(u));
        saveToFile();
        return true;
    }

    @Override
    public synchronized boolean elimina(Long id) {
        if (id == null) return false;
        boolean removed = utenti.removeIf(u -> id.equals(u.getId()));
        if (removed) {
            credenziali.removeIf(c -> id.equals(c.userId));
            saveToFile();
        }
        return removed;
    }

    @Override
    public synchronized boolean creaCredenziali(Long utenteId, String username, String passwordPlain) {
        if (utenteId == null || isBlank(username) || isBlank(passwordPlain)) return false;
        Optional<CredRow> existing = findCredRowByUserId(utenteId);
        if (existing.isPresent()) {
            return false;
        }
        String hash = sha256Hex(passwordPlain);
        credenziali.add(new CredRow(utenteId, username, hash, "UTENTE"));
        saveToFile();
        return true;
    }

    @Override
    public synchronized boolean aggiornaCredenziali(Long utenteId, String username, String passwordPlain) {
        if (utenteId == null || isBlank(username) || isBlank(passwordPlain)) return false;
        String hash = sha256Hex(passwordPlain);

        Optional<CredRow> existing = findCredRowByUserId(utenteId);
        if (existing.isPresent()) {
            CredRow row = existing.get();
            row.username = username;
            row.passwordHash = hash;
        } else {
            credenziali.add(new CredRow(utenteId, username, hash, "UTENTE"));
        }

        saveToFile();
        return true;
    }

    @Override
    public synchronized Optional<String> getUsernameForUserId(Long utenteId) {
        Optional<CredRow> cr = findCredRowByUserId(utenteId);
        return cr.map(c -> c.username);
    }

    @Override
    public synchronized Optional<AuthData> findAuthByUsername(String username) {
        if (isBlank(username)) return Optional.empty();

        Optional<CredRow> crOpt = findCredRowByUsername(username);
        if (crOpt.isEmpty()) return Optional.empty();

        CredRow cr = crOpt.get();
        Utente ut = findUserByIdInternal(cr.userId);
        if (ut == null) return Optional.empty();

        UtenteDAO.AuthData authData = new UtenteDAO.AuthData(
                cr.username,
                cr.passwordHash,
                cr.role,
                ut.getId(),
                ut.getTessera()
        );
        return Optional.of(authData);
    }

    private synchronized void loadFromFile() {
        utenti.clear();
        credenziali.clear();
        userSeq = 0L;

        if (!usersFile.exists()) {
            seedDefaults();
            saveToFile();
            return;
        }

        String json;
        try {
            json = Files.readString(usersFile.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            // Se non riesco a leggere il file, faccio fallback ai default.
            LOGGER.log(Level.WARNING, "Impossibile leggere utenti.json, uso valori di default.", e);
            seedDefaults();
            saveToFile();
            return;
        }

        parseUsersArray(json);

        for (Utente u : utenti) {
            if (u.getId() != null && u.getId() > userSeq) {
                userSeq = u.getId();
            }
        }

        if (utenti.isEmpty()) {
            LOGGER.warning("utenti.json vuoto o malformato, ricreo dati di default.");
            seedDefaults();
            saveToFile();
        }
    }

    private void seedDefaults() {
        utenti.clear();
        credenziali.clear();

        LocalDate oggi = LocalDate.now();

        addSeedUser(
                1L,
                0,
                "Admin",
                "Admin",
                "admin@biblioteca.local",
                "0000000000",
                oggi,
                oggi.plusYears(10),
                "admin",
                "8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918",
                "ADMIN"
        );

        addSeedUser(
                2L,
                1,
                "Bibliotecario",
                "Bibliotecario",
                "bibliotecario@biblioteca.local",
                "0000000000",
                oggi,
                oggi.plusYears(5),
                "bibliotecario",
                "8e133e19f9bcdc034fa2ea1af34b47337eeb7d951ad4fb32ad502ccbaf4f8d52",
                "BIBLIOTECARIO"
        );

        addSeedUser(
                3L,
                100,
                "Mario",
                "Rossi",
                "mario.rossi@example.local",
                "3331112233",
                oggi,
                oggi.plusYears(2),
                "mario",
                "59195c6c541c8307f1da2d1e768d6f2280c984df217ad5f4c64c3542b04111a4",
                "UTENTE"
        );

        addSeedUser(
                4L,
                101,
                "Giulia",
                "Bianchi",
                "giulia.bianchi@example.local",
                "3332223344",
                oggi,
                oggi.plusYears(2),
                "giulia",
                "e4c2eed8a6df0147265631e9ff25b70fd0e4b3a246896695b089584bf3ce8b90",
                "UTENTE"
        );

        addSeedUser(
                5L,
                102,
                "Luca",
                "Verdi",
                "luca.verdi@example.local",
                "3334445566",
                oggi,
                oggi.plusYears(2),
                "luca",
                "d70f47790f689414789eeff231703429c7f88a10210775906460edbf38589d90",
                "UTENTE"
        );

        userSeq = 5L;
    }

    private void addSeedUser(
            Long id,
            Integer tessera,
            String nome,
            String cognome,
            String email,
            String telefono,
            LocalDate att,
            LocalDate scad,
            String username,
            String passwordHash,
            String role
    ) {
        Utente u = new Utente();
        u.setId(id);
        u.setTessera(tessera);
        u.setNome(nome);
        u.setCognome(cognome);
        u.setEmail(email);
        u.setTelefono(telefono);
        u.setDataAttivazione(att);
        u.setDataScadenza(scad);
        utenti.add(u);

        credenziali.add(new CredRow(id, username, passwordHash, role));
    }

    private void parseUsersArray(String json) {
        if (json == null) return;
        String trimmed = json.trim();
        if (trimmed.isEmpty() || "[]".equals(trimmed)) return;

        List<String> objs = splitTopLevelObjects(trimmed);
        for (String obj : objs) {
            Long id = extractLongField(obj, "id");
            Integer tess = extractIntField(obj, "tessera");
            String nome = extractStringField(obj, "nome");
            String cognome = extractStringField(obj, "cognome");
            String email = extractStringField(obj, "email");
            String telefono = extractStringField(obj, "telefono");
            LocalDate att = parseDate(extractStringField(obj, "dataAttivazione"));
            LocalDate scad = parseDate(extractStringField(obj, "dataScadenza"));
            String username = extractStringField(obj, "username");
            String passHash = extractStringField(obj, "passwordHash");
            String role = extractStringField(obj, "role");

            Utente u = new Utente();
            if (id != null) u.setId(id);
            if (tess != null) u.setTessera(tess);
            u.setNome(nome);
            u.setCognome(cognome);
            u.setEmail(email);
            u.setTelefono(telefono);
            u.setDataAttivazione(att);
            u.setDataScadenza(scad);

            utenti.add(u);
            credenziali.add(new CredRow(id, username, passHash, role));
        }
    }

    private synchronized void saveToFile() {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int idx = 0; idx < utenti.size(); idx++) {
            Utente u = utenti.get(idx);
            CredRow c = findCredRowByUserIdInternal(u.getId());

            sb.append("  {\n");
            sb.append("    \"id\": ").append(u.getId() == null ? "null" : u.getId()).append(",\n");
            sb.append("    \"tessera\": ").append(u.getTessera() == null ? "null" : u.getTessera()).append(",\n");
            sb.append("    \"nome\": ").append(toJsonString(u.getNome())).append(",\n");
            sb.append("    \"cognome\": ").append(toJsonString(u.getCognome())).append(",\n");
            sb.append("    \"email\": ").append(toJsonString(u.getEmail())).append(",\n");
            sb.append("    \"telefono\": ").append(toJsonString(u.getTelefono())).append(",\n");
            sb.append("    \"dataAttivazione\": ").append(toJsonString(u.getDataAttivazione() != null ? u.getDataAttivazione().toString() : null)).append(",\n");
            sb.append("    \"dataScadenza\": ").append(toJsonString(u.getDataScadenza() != null ? u.getDataScadenza().toString() : null)).append(",\n");

            if (c != null) {
                sb.append("    \"username\": ").append(toJsonString(c.username)).append(",\n");
                sb.append("    \"passwordHash\": ").append(toJsonString(c.passwordHash)).append(",\n");
                sb.append("    \"role\": ").append(toJsonString(c.role)).append("\n");
            } else {
                sb.append("    \"username\": null,\n");
                sb.append("    \"passwordHash\": null,\n");
                sb.append("    \"role\": null\n");
            }

            sb.append("  }");
            if (idx < utenti.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("]");

        try {
            Files.writeString(usersFile.toPath(), sb.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore durante il salvataggio di utenti.json", e);
        }
    }

    private static List<String> splitTopLevelObjects(String json) {
        List<String> out = new ArrayList<>();
        int len = json.length();
        boolean inString = false;
        int depth = 0;
        int start = -1;

        for (int i = 0; i < len; i++) {
            char ch = json.charAt(i);

            if (ch == '"') {
                boolean escaped = false;
                int j = i - 1;
                while (j >= 0 && json.charAt(j) == '\\') {
                    escaped = !escaped;
                    j--;
                }
                if (!escaped) {
                    inString = !inString;
                }
            }

            if (!inString) {
                if (ch == '{') {
                    if (depth == 0) {
                        start = i;
                    }
                    depth++;
                } else if (ch == '}') {
                    depth--;
                    if (depth == 0 && start >= 0) {
                        out.add(json.substring(start, i + 1));
                        start = -1;
                    }
                }
            }
        }
        return out;
    }

    private static String extractStringField(String obj, String field) {
        Pattern p = Pattern.compile("\"" + Pattern.quote(field) + "\"\\s*:\\s*\"(.*?)\"", Pattern.DOTALL);
        Matcher m = p.matcher(obj);
        if (m.find()) {
            return jsonUnescape(m.group(1));
        }
        return null;
    }

    private static Long extractLongField(String obj, String field) {
        Pattern p = Pattern.compile("\"" + Pattern.quote(field) + "\"\\s*:\\s*(\\d+)");
        Matcher m = p.matcher(obj);
        if (m.find()) {
            try {
                return Long.valueOf(m.group(1));
            } catch (Exception ignored) { }
        }
        return null;
    }

    private static Integer extractIntField(String obj, String field) {
        Pattern p = Pattern.compile("\"" + Pattern.quote(field) + "\"\\s*:\\s*(\\d+)");
        Matcher m = p.matcher(obj);
        if (m.find()) {
            try {
                return Integer.valueOf(m.group(1));
            } catch (Exception ignored) { }
        }
        return null;
    }

    private static LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return LocalDate.parse(s.trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String toJsonString(String s) {
        if (s == null) return "null";
        return "\"" + jsonEscape(s) + "\"";
    }

    private static String jsonEscape(String s) {
        if (s == null) return "";
        StringBuilder b = new StringBuilder();
        for (char ch : s.toCharArray()) {
            if (ch == '\\') {
                b.append("\\\\");
            } else if (ch == '"') {
                b.append("\\\"");
            } else {
                b.append(ch);
            }
        }
        return b.toString();
    }

    private static String jsonUnescape(String s) {
        if (s == null) return null;
        StringBuilder out = new StringBuilder();
        boolean esc = false;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (esc) {
                out.append(ch);
                esc = false;
            } else {
                if (ch == '\\') {
                    esc = true;
                } else {
                    out.append(ch);
                }
            }
        }
        return out.toString();
    }

    private static Comparator<String> nullSafeString() {
        return Comparator.nullsFirst(String::compareToIgnoreCase);
    }

    private int findIndexById(Long id) {
        for (int i = 0; i < utenti.size(); i++) {
            Utente u = utenti.get(i);
            if (Objects.equals(id, u.getId())) return i;
        }
        return -1;
    }

    private Utente findUserByIdInternal(Long id) {
        if (id == null) return null;
        for (Utente u : utenti) {
            if (Objects.equals(id, u.getId())) return u;
        }
        return null;
    }

    private Optional<CredRow> findCredRowByUserId(Long userId) {
        return Optional.ofNullable(findCredRowByUserIdInternal(userId));
    }

    private CredRow findCredRowByUserIdInternal(Long userId) {
        for (CredRow c : credenziali) {
            if (Objects.equals(userId, c.userId)) {
                return c;
            }
        }
        return null;
    }

    private Optional<CredRow> findCredRowByUsername(String username) {
        for (CredRow c : credenziali) {
            if (c.username != null && c.username.equalsIgnoreCase(username)) {
                return Optional.of(c);
            }
        }
        return Optional.empty();
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static Utente cloneUtente(Utente src) {
        if (src == null) return null;
        Utente u = new Utente();
        u.setId(src.getId());
        u.setTessera(src.getTessera());
        u.setNome(src.getNome());
        u.setCognome(src.getCognome());
        u.setEmail(src.getEmail());
        u.setTelefono(src.getTelefono());
        u.setDataAttivazione(src.getDataAttivazione());
        u.setDataScadenza(src.getDataScadenza());
        return u;
    }

    private static String sha256Hex(String rawPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 non disponibile", e);
        }
    }
}