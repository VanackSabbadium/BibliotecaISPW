package it.biblioteca.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test "fail fast" per AuthService.authenticate:
 * - quando il backend non è configurato o non accessibile,
 *   l'autenticazione deve fallire senza lanciare eccezioni.
 *
 * Non testiamo il path "success" perché richiederebbe un backend reale con dati.
 */
class AuthServiceTest {

    @Test
    void authenticate_returnsFailWhenBackendUnavailable() {
        AuthService.AuthResult res = AuthService.authenticate("any", "any");

        assertNotNull(res);
        assertFalse(res.ok(), "Se il backend non è configurato/raggiungibile, deve fallire in modo sicuro");

        // In caso di fallimento non devono esserci info utente
        assertNull(res.role());
        assertNull(res.userId());
        assertNull(res.tessera());
    }

    @Test
    void authenticate_nullOrBlankInputFails() {
        assertFalse(AuthService.authenticate(null, "x").ok());
        assertFalse(AuthService.authenticate(" ", "x").ok());
        assertFalse(AuthService.authenticate("user", null).ok());
    }
}