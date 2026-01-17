package it.biblioteca.dao.db;

import it.biblioteca.bean.PrestitoBean;
import it.biblioteca.dao.PrestitoDAO;
import it.biblioteca.entity.Prestito;
import it.biblioteca.testutil.TestConnectionProvider;
import it.biblioteca.testutil.TestDbSetup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PrestitoFlowDbTest {

    private TestConnectionProvider cp;
    private PrestitoDAO dao;

    @BeforeEach
    void setup() throws Exception {
        cp = new TestConnectionProvider();
        try (Connection c = cp.getConnection()) {
            TestDbSetup.resetSchema(c);
        }
        dao = new DbPrestitoDAO(cp);
    }

    @Test
    void registraEChiudiPrestito() {
        // Abbiamo già un libro seed con id=1 e un utente Mario con id=3 in setup.
        PrestitoBean bean = new PrestitoBean();
        bean.setLibroId(1L);
        bean.setUtenteId(3L);
        bean.setLibroTitoloSnapshot("Libro Test");
        bean.setUtenteSnapshot("Mario Rossi");
        bean.setDataPrestito(LocalDate.of(2025, 1, 10));

        assertTrue(dao.inserisci(bean));

        List<Prestito> tutti = dao.trovaTutti();
        assertEquals(1, tutti.size());
        Prestito p = tutti.getFirst();
        assertNotNull(p.getId());
        assertEquals(1L, p.getLibroId());
        assertEquals(3L, p.getUtenteId());
        assertEquals("Mario Rossi", p.getUtente());
        assertEquals("Libro Test", p.getLibroTitoloSnapshot());
        assertEquals(LocalDate.of(2025, 1, 10), p.getDataPrestito());
        assertNull(p.getDataRestituzione());

        // Attivi
        assertEquals(1, dao.trovaPrestitiAttivi().size());

        // Chiudi
        assertTrue(dao.chiudiPrestito(p.getId(), LocalDate.of(2025, 1, 20)));
        List<Prestito> attivi = dao.trovaPrestitiAttivi();
        assertEquals(0, attivi.size());

        // Verifica data restituzione
        Prestito closed = dao.trovaTutti().getFirst();
        assertEquals(LocalDate.of(2025, 1, 20), closed.getDataRestituzione());
    }
}
