package it.biblioteca.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SessionContextTest {

    @AfterEach
    void reset() {
        // reset "soft" dello stato condiviso
        SessionContext.setRole(null);
        SessionContext.setUserId();
        SessionContext.setTessera(null);
        // niente setAuthenticatedUsername(): non esiste più
    }

    @Test
    void rolesAreRecognized() {
        SessionContext.setRole("ADMIN");
        assertTrue(SessionContext.isAdmin());
        assertFalse(SessionContext.isBibliotecario());
        assertFalse(SessionContext.isUtente());

        SessionContext.setRole("BIBLIOTECARIO");
        assertFalse(SessionContext.isAdmin());
        assertTrue(SessionContext.isBibliotecario());
        assertFalse(SessionContext.isUtente());

        SessionContext.setRole("UTENTE");
        assertFalse(SessionContext.isAdmin());
        assertFalse(SessionContext.isBibliotecario());
        assertTrue(SessionContext.isUtente());
    }

    @Test
    void tesseraRoundtrip() {
        SessionContext.setTessera(42);
        assertEquals(42, SessionContext.getTessera());
        SessionContext.setTessera(null);
        assertNull(SessionContext.getTessera());
    }
}