package it.biblioteca;

import it.biblioteca.dao.ConnectionProvider;
import it.biblioteca.dao.DatabaseConnectionProvider;
import it.biblioteca.dao.DaoFactory;
import it.biblioteca.dao.db.DbDaoFactory;
import it.biblioteca.dao.json.JsonDaoFactory;
import it.biblioteca.security.AuthService;
import it.biblioteca.security.SessionContext;
import it.biblioteca.service.AppContext;
import it.biblioteca.ui.ContentManager;
import it.biblioteca.ui.facade.UiFacade;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;
import java.util.Optional;

public class BibliotecaApp extends Application {

    enum Backend { DB, FILE }

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();

        Backend backend = askBackend();
        DaoFactory factory;

        if (backend == Backend.FILE) {
            File dir = askJsonDirectory(stage);
            factory = new JsonDaoFactory(dir);
        } else {
            ConnectionProvider cp = new DatabaseConnectionProvider();
            factory = new DbDaoFactory(cp);
        }

        AuthService.init(factory);
        SessionContext.setDaoFactory(factory);

        AppContext ctx = new AppContext(factory);
        UiFacade ui = UiFacade.from(ctx);

        ContentManager contentManager = new ContentManager(ui);
        contentManager.inizializzaContenuto(root);

        Scene scene = new Scene(root, 1100, 740);
        stage.setTitle("Biblioteca");
        stage.setScene(scene);
        stage.show();
    }

    private Backend askBackend() {
        ChoiceDialog<Backend> dialog = new ChoiceDialog<>(Backend.DB, List.of(Backend.DB, Backend.FILE));
        dialog.setTitle("Seleziona backend");
        dialog.setHeaderText("Scegli il backend dati");
        dialog.setContentText("Usare DB o File JSON?");
        Optional<Backend> res = dialog.showAndWait();
        return res.orElse(Backend.DB);
    }

    private File askJsonDirectory(Stage owner) {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Scegli cartella dati JSON");
        File dir = dc.showDialog(owner);
        if (dir == null) {
            dir = new File("data");
            if (!dir.exists()) dir.mkdirs();
        }
        return dir;
    }
}