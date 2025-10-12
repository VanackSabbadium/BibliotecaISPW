package it.biblioteca.db;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test "puri" su DatabaseConfig che non richiedono accesso al DB.
 * Usiamo reflection per simulare lo stato di configurazione senza chiamare StartupDialog.
 */
class DatabaseConfigTest {

    @AfterEach
    void resetConfig() throws Exception {
        setPrivateStatic("username", null);
        setPrivateStatic("password", null);
    }

    @Test
    void buildJdbcUrl_isDeterministic() {
        String url = DatabaseConfig.buildJdbcUrl();
        assertEquals("jdbc:mariadb://localhost:3306/biblioteca", url);
    }

    @Test
    void isConfigured_falseWhenMissingCreds() throws Exception {
        setPrivateStatic("username", null);
        setPrivateStatic("password", null);
        assertFalse(DatabaseConfig.isConfigured());
    }

    @Test
    void isConfigured_trueWhenBothPresent() throws Exception {
        setPrivateStatic("username", "user");
        setPrivateStatic("password", "pass");
        assertTrue(DatabaseConfig.isConfigured());
    }

    // Helpers --------------------------------------------------------------

    private static void setPrivateStatic(String field, Object value) throws Exception {
        Field f = DatabaseConfig.class.getDeclaredField(field);
        f.setAccessible(true);
        f.set(null, value);
    }
}
