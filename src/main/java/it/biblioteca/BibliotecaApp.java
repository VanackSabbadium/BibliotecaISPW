package it.biblioteca;

import it.biblioteca.controller.BookController;
import it.biblioteca.controller.PrestitoController;
import it.biblioteca.controller.UtenteController;

import it.biblioteca.dao.BookDAO;
import it.biblioteca.dao.PrestitoDAO;
import it.biblioteca.dao.UtenteDAO;
import it.biblioteca.dao.jdbc.JdbcBookDAO;
import it.biblioteca.dao.jdbc.JdbcPrestitoDAO;
import it.biblioteca.dao.jdbc.JdbcUtenteDAO;

import it.biblioteca.ui.ContentManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Bootstrap dell'applicazione:
 * - Nessun prompt su terminale per le credenziali (gestite via StartupDialog).
 * - I controller ricevono implementazioni concrete dei DAO.
 */
public class BibliotecaApp extends Application {

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();

        // DAO concreti (JDBC) che rispettano le interfacce fornite
        UtenteDAO utenteDAO = new JdbcUtenteDAO();
        PrestitoDAO prestitoDAO = new JdbcPrestitoDAO();
        BookDAO bookDAO = new JdbcBookDAO();

        // Controller con le dipendenze richieste
        UtenteController utenteController = new UtenteController(utenteDAO);
        PrestitoController prestitoController = new PrestitoController(prestitoDAO, utenteController);
        BookController bookController = new BookController(bookDAO, prestitoController);

        // UI
        ContentManager contentManager = new ContentManager(bookController, prestitoController, utenteController);
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