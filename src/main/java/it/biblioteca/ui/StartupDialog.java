package it.biblioteca.ui;

import it.biblioteca.ui.ContentManager.Theme;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.Optional;

public class StartupDialog extends Dialog<StartupResult> {

    public StartupDialog() {
        setTitle("Accesso | Biblioteca");
        setHeaderText("Inserisci le credenziali e scegli il tema grafico");

        ButtonType okButtonType = new ButtonType("Accedi", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));

        TextField txtUser = new TextField();
        txtUser.setPromptText("Nome utente (Bibliotecario o Utente)");

        PasswordField txtPass = new PasswordField();
        txtPass.setPromptText("Password");

        ComboBox<Theme> cmbTheme = new ComboBox<>();
        cmbTheme.getItems().addAll(Theme.COLORI, Theme.BIANCO_NERO);
        cmbTheme.getSelectionModel().select(Theme.COLORI);

        TextField txtTessera = new TextField();
        txtTessera.setPromptText("Numero tessera (richiesto per 'Utente')");
        txtTessera.setDisable(true);

        grid.add(new Label("Utente:"), 0, 0);
        grid.add(txtUser, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(txtPass, 1, 1);
        grid.add(new Label("Tema:"), 0, 2);
        grid.add(cmbTheme, 1, 2);
        grid.add(new Label("Tessera:"), 0, 3);
        grid.add(txtTessera, 1, 3);

        getDialogPane().setContent(grid);

        Node okBtn = getDialogPane().lookupButton(okButtonType);
        okBtn.setDisable(true);

        Runnable validate = () -> {
            String u = txtUser.getText();
            String p = txtPass.getText();
            boolean needTessera = (u != null && u.equalsIgnoreCase("Utente"));
            txtTessera.setDisable(!needTessera);
            boolean valid = u != null && !u.isBlank()
                    && p != null && !p.isBlank()
                    && (!needTessera || (txtTessera.getText() != null && !txtTessera.getText().isBlank()));
            okBtn.setDisable(!valid);
        };

        txtUser.textProperty().addListener((o, a, b) -> validate.run());
        txtPass.textProperty().addListener((o, a, b) -> validate.run());
        txtTessera.textProperty().addListener((o, a, b) -> validate.run());
        validate.run();

        setResultConverter(bt -> {
            if (bt == okButtonType) {
                Integer tessera = null;
                String u = txtUser.getText();
                if (u != null && u.equalsIgnoreCase("Utente")) {
                    try {
                        tessera = Integer.parseInt(txtTessera.getText().trim());
                    } catch (NumberFormatException ex) {
                        return null;
                    }
                }
                return new StartupResult(txtUser.getText().trim(),
                        txtPass.getText(),
                        cmbTheme.getValue(),
                        tessera);
            }
            return null;
        });
    }

    // Importante: non sovrascrivere showAndWait(). Usa direttamente il metodo ereditato:
    // Optional<StartupResult> result = new StartupDialog().showAndWait();
}