package it.biblioteca;

import it.biblioteca.dao.ConnectionProvider;
import it.biblioteca.dao.DatabaseConnectionProvider;
import it.biblioteca.service.AppContext;
import it.biblioteca.ui.ContentManager;
import it.biblioteca.ui.facade.UiFacade;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class BibliotecaApp extends Application {
    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();

        ConnectionProvider cp = new DatabaseConnectionProvider();
        AppContext ctx = new AppContext(cp);
        UiFacade ui = UiFacade.from(ctx);

        ContentManager contentManager = new ContentManager(ui);
        contentManager.inizializzaContenuto(root);

        Scene scene = new Scene(root, 1100, 740);
        stage.setTitle("Biblioteca");
        stage.setScene(scene);
        stage.show();
    }
}
