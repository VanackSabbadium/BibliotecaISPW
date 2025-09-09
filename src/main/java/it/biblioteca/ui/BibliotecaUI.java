package it.biblioteca.ui;

import it.biblioteca.controller.BookController;
import it.biblioteca.controller.PrenotazioneController;
import it.biblioteca.controller.PrestitoController;
import it.biblioteca.controller.UtenteController;
import it.biblioteca.dao.BookDAO;
import it.biblioteca.dao.CredenzialiDAO;
import it.biblioteca.dao.PrenotazioneDAO;
import it.biblioteca.dao.PrestitoDAO;
import it.biblioteca.dao.UtenteDAO;
import it.biblioteca.dao.jdbc.JdbcBookDAO;
import it.biblioteca.dao.jdbc.JdbcCredenzialiDAO;
import it.biblioteca.dao.jdbc.JdbcPrestitoDAO;
import it.biblioteca.dao.jdbc.JdbcUtenteDAO;
import it.biblioteca.dao.jdbc.JdbcPrenotazioneDAO; // rimuovi se non esiste
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Alternativa a BibliotecaApp che incapsula la creazione di DAO/controller e
 * l'inizializzazione della UI in un'istanza riutilizzabile.
 */
public class BibliotecaUI {

    private final Stage stage;
    private final BorderPane root;
    private final ContentManager contentManager;

    public BibliotecaUI(Stage stage) {
        this.stage = stage;
        this.root = new BorderPane();

        // Istanzia DAO
        UtenteDAO utenteDAO = new JdbcUtenteDAO();
        PrestitoDAO prestitoDAO = new JdbcPrestitoDAO();
        BookDAO bookDAO = new JdbcBookDAO();
        CredenzialiDAO credenzialiDAO = new JdbcCredenzialiDAO();

        PrenotazioneDAO prenotazioneDAO = null;
        try {
            prenotazioneDAO = new JdbcPrenotazioneDAO();
        } catch (NoClassDefFoundError | Exception ignored) {
            prenotazioneDAO = null;
        }

        // Istanzia controller
        UtenteController utenteController = new UtenteController(utenteDAO, credenzialiDAO);
        PrestitoController prestitoController = new PrestitoController(prestitoDAO, utenteController);

        PrenotazioneController prenotazioneController = null;
        if (prenotazioneDAO != null) {
            prenotazioneController = new PrenotazioneController(prenotazioneDAO);        }

        BookController bookController = new BookController(bookDAO, prestitoController);

        // Costruisci ContentManager con o senza prenotazioni
        if (prenotazioneController != null) {
            contentManager = new ContentManager(bookController, prestitoController, utenteController, prenotazioneController);
        } else {
            contentManager = new ContentManager(bookController, prestitoController, utenteController);
        }
    }

    /**
     * Inizializza e mostra la UI.
     */
    public void show() {
        contentManager.inizializzaContenuto(root);
        Scene scene = new Scene(root, 1100, 740);
        stage.setTitle("Biblioteca");
        stage.setScene(scene);
        stage.show();
    }
}
