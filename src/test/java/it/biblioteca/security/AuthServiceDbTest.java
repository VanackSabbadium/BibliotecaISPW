package it.biblioteca.security;

import it.biblioteca.dao.DaoFactory;
import it.biblioteca.dao.db.DbDaoFactory;
import it.biblioteca.security.SessionContext.AppRole;
import it.biblioteca.testutil.TestConnectionProvider;
import it.biblioteca.testutil.TestDbSetup;
import org.junit.jupiter.api.*;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceDbTest {

    private static TestConnectionProvider cp;
    private static DaoFactory factory;

    @BeforeAll
    static void init() throws Exception {
        cp = new TestConnectionProvider();
        try (Connection c = cp.getConnection()) {
            TestDbSetup.resetSchema(c);
        }
        factory = new DbDaoFactory(cp);
        AuthService.init(factory);
    }

    @AfterEach
    void resetSession() {
        SessionContext.setRole(null);
        SessionContext.setUserId();
        SessionContext.setTessera(null);
    }

    @Test
    void loginAdmin_ok_e_wrongPassword_fail() {
        AuthService.AuthResult ok = AuthService.authenticate("admin", "admin");
        assertTrue(ok.ok());
        assertEquals(AppRole.ADMIN, ok.role());
        assertNotNull(ok.userId());
        assertEquals(0, ok.tessera());

        AuthService.AuthResult fail = AuthService.authenticate("admin", "xxx");
        assertFalse(fail.ok());
    }

    @Test
    void loginUnknownUser_fail() {
        AuthService.AuthResult r = AuthService.authenticate("sconosciuto", "whatever");
        assertFalse(r.ok());
    }
}
