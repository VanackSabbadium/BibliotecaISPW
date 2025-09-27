package it.biblioteca.ui;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

public class StartupDialog extends Dialog<StartupResult> {

    public StartupDialog() {
        setTitle("Avvio - Configurazione");
        setHeaderText("Configurazione DB e login applicativo");

        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));

        TextField dbUser = new TextField("Admin");
        dbUser.setDisable(true);
        PasswordField dbPass = new PasswordField();
        dbPass.setText("admin");
        dbPass.setDisable(true);

        TextField appUser = new TextField();
        appUser.setPromptText("username applicativo");
        PasswordField appPass = new PasswordField();
        appPass.setPromptText("password applicativa");

        ComboBox<ContentManager.Theme> cmbTheme = new ComboBox<>();
        cmbTheme.getItems().addAll(ContentManager.Theme.COLORI, ContentManager.Theme.BIANCO_NERO);
        cmbTheme.getSelectionModel().select(ContentManager.Theme.COLORI);

        grid.add(new Label("DB Username:"), 0, 0);
        grid.add(dbUser, 1, 0);
        grid.add(new Label("DB Password:"), 0, 1);
        grid.add(dbPass, 1, 1);

        grid.add(new Label("Username (app):"), 0, 2);
        grid.add(appUser, 1, 2);
        grid.add(new Label("Password (app):"), 0, 3);
        grid.add(appPass, 1, 3);

        grid.add(new Label("Tema:"), 0, 4);
        grid.add(cmbTheme, 1, 4);

        getDialogPane().setContent(grid);

        Node okButton = getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(true);

        Runnable validate = () -> okButton.setDisable(
                appUser.getText() == null || appUser.getText().trim().isEmpty()
                        || appPass.getText() == null || appPass.getText().trim().isEmpty()
        );
        appUser.textProperty().addListener((obs, o, n) -> validate.run());
        appPass.textProperty().addListener((obs, o, n) -> validate.run());
        validate.run();

        setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                return new StartupResult(
                        dbUser.getText(),                // username DB (Admin)
                        dbPass.getText(),                // password DB
                        appUser.getText().trim(),        // username applicativo
                        appPass.getText(),               // password applicativa
                        cmbTheme.getValue()              // tema
                );
            }
            return null;
        });
    }
}
