package it.biblioteca;

import it.biblioteca.controller.BookController;
import it.biblioteca.controller.PrenotazioneController;
import it.biblioteca.controller.PrestitoController;
import it.biblioteca.controller.UtenteController;
import it.biblioteca.dao.BookDAO;
import it.biblioteca.dao.PrenotazioneDAO;
import it.biblioteca.dao.PrestitoDAO;
import it.biblioteca.dao.UtenteDAO;
import it.biblioteca.dao.CredenzialiDAO;
import it.biblioteca.dao.jdbc.JdbcBookDAO;
import it.biblioteca.dao.jdbc.JdbcPrestitoDAO;
import it.biblioteca.dao.jdbc.JdbcUtenteDAO;
import it.biblioteca.dao.jdbc.JdbcCredenzialiDAO;
import it.biblioteca.dao.jdbc.JdbcPrenotazioneDAO;
import it.biblioteca.ui.ContentManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Bootstrap principale dell'applicazione.
 * Crea DAO concreti (JDBC), controller e avvia la UI.
 *
 * Nota: uso le implementazioni JDBC (default ctor) che leggono la DatabaseConfig centrale.
 */
public class BibliotecaApp extends Application {

    @Override
    public void start(Stage stage) {
        // Root container (usato come parametro per inizializzare la UI)
        BorderPane root = new BorderPane();

        // ----------------------------
        // Istanzia DAO concreti (JDBC)
        // ----------------------------
        // Le classi Jdbc* utilizzano internamente la configurazione centrale del DB
        // tramite it.biblioteca.db.DatabaseConfig.getConnection() / getServiceConnection().
        UtenteDAO utenteDAO = new JdbcUtenteDAO();
        PrestitoDAO prestitoDAO = new JdbcPrestitoDAO();
        BookDAO bookDAO = new JdbcBookDAO();
        CredenzialiDAO credenzialiDAO = new JdbcCredenzialiDAO();

        // Prenotazione: se non hai ancora la classe JdbcPrenotazioneDAO,
        // commenta la riga sottostante e rimuovi la dipendenza in seguito.
        PrenotazioneDAO prenotazioneDAO = null;
        try {
            // se esiste la classe JdbcPrenotazioneDAO la useremo
            prenotazioneDAO = new JdbcPrenotazioneDAO();
        } catch (NoClassDefFoundError | Exception ignored) {
            // ignoro: l'app può funzionare anche senza prenotazioni (adatta il ContentManager se necessario)
            prenotazioneDAO = null;
        }

        // ----------------------------
        // Istanzia controller
        // ----------------------------
        // UtenteController ora richiede anche il CredenzialiDAO (per creazione/gestione credenziali)
        UtenteController utenteController = new UtenteController(utenteDAO, credenzialiDAO);

        // Prestito controller ha bisogno di utenteController per validazioni utente
        PrestitoController prestitoController = new PrestitoController(prestitoDAO, utenteController);

        // Prenotazione controller (se presente)
        PrenotazioneController prenotazioneController = null;
        if (prenotazioneDAO != null) {
            try {
                prenotazioneController = new PrenotazioneController(prenotazioneDAO);
            } catch (NoClassDefFoundError | Exception ignored) {
                prenotazioneController = null;
            }
        }

        // BookController usa prestitoController per verificare disponibilità
        BookController bookController = new BookController(bookDAO, prestitoController);

        // ----------------------------
        // UI: ContentManager
        // ----------------------------
        // ContentManager è responsabile di inizializzare la UI e gestire i tab.
        // Presuppongo che abbia un costruttore che accetta:
        // (BookController, PrestitoController, UtenteController) o la variante che include PrenotazioneController.
        ContentManager contentManager;
        if (prenotazioneController != null) {
            // versione con prenotazioni
            contentManager = new ContentManager(bookController, prestitoController, utenteController, prenotazioneController);
        } else {
            // versione senza prenotazioni (fallback)
            contentManager = new ContentManager(bookController, prestitoController, utenteController);
        }

        // inizializza la UI passando il root BorderPane (qui verrà mostrato il TabPane ecc.)
        contentManager.inizializzaContenuto(root);

        Scene scene = new Scene(root, 1100, 740);
        stage.setTitle("Biblioteca");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
