package it.biblioteca.ui;

import it.biblioteca.config.DatabaseConfig;
import it.biblioteca.controller.BookController;
import it.biblioteca.controller.PrestitoController;
import it.biblioteca.controller.UtenteController;
import it.biblioteca.dao.*;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class BibliotecaUI {
    private final Stage stage;
    private final BorderPane root;
    private final BookController bookController;
    private final PrestitoController prestitoController;
    private final UtenteController utenteController;
    private final NavigationManager navigationManager;
    private final ContentManager contentManager;

    public BibliotecaUI(Stage stage, DatabaseConfig dbConfig) {
        this.stage = stage;
        this.root = new BorderPane();

        // DAO
        PrestitoDAO prestitoDAO = new PrestitoDAOImpl(dbConfig);
        BookDAO bookDAO = new BookDAOImpl(dbConfig);
        UtenteDAO utenteDAO = new UtenteDAOImpl(dbConfig);

        // Controller: crea prima UtenteController, poi PrestitoController (che ora lo richiede), poi BookController
        utenteController = new UtenteController(utenteDAO);
        prestitoController = new PrestitoController(prestitoDAO, utenteController);
        bookController = new BookController(bookDAO, prestitoController);

        // UI Managers
        navigationManager = new NavigationManager();
        contentManager = new ContentManager(bookController, prestitoController, utenteController);
    }

    public void inizializzaInterfaccia() {
        stage.setTitle("Gestione Biblioteca");
        Scene scene = new Scene(root, 1100, 750);

        navigationManager.inizializzaNavigazione(root);
        contentManager.inizializzaContenuto(root);

        navigationManager.setHomeButtonAction(e -> contentManager.mostraHome());
        navigationManager.setBookButtonAction(e -> contentManager.mostraCatalogoLibri());
        navigationManager.setLoanButtonAction(e -> contentManager.mostraPrestiti());
        navigationManager.setUserButtonAction(e -> contentManager.mostraUtenti());

        stage.setScene(scene);
        stage.show();
    }
}