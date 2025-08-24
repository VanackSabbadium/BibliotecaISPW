package it.biblioteca.ui;

import it.biblioteca.ui.ContentManager.Theme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

public class StartupDialog extends Dialog<StartupResult> {

    public StartupDialog() {
        setTitle("Accesso");
        setHeaderText("Inserisci username e password e scegli il tema dell'interfaccia");

        ButtonType confirmBtn = new ButtonType("Conferma", ButtonBar.ButtonData.OK_DONE);
        ButtonType closeBtn   = new ButtonType("Chiudi", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(confirmBtn, closeBtn);

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField userField = new TextField();
        userField.setPromptText("Nome utente");

        PasswordField pwdField = new PasswordField();
        pwdField.setPromptText("Password");

        Label userLbl = new Label("Utente:");
        Label pwdLbl  = new Label("Password:");

        // Selettore tema: radio button con ToggleGroup
        Label themeLbl = new Label("Tema:");
        ToggleGroup tg = new ToggleGroup();
        RadioButton rbColori = new RadioButton("Colori");
        rbColori.setToggleGroup(tg);
        rbColori.setSelected(true);
        RadioButton rbBN = new RadioButton("Bianco e Nero");
        rbBN.setToggleGroup(tg);

        GridPane themeRow = new GridPane();
        themeRow.setHgap(10);
        themeRow.add(rbColori, 0, 0);
        themeRow.add(rbBN, 1, 0);

        int r = 0;
        grid.add(userLbl, 0, r);
        grid.add(userField, 1, r++);
        grid.add(pwdLbl, 0, r);
        grid.add(pwdField, 1, r++);
        grid.add(themeLbl, 0, r);
        grid.add(themeRow, 1, r);

        getDialogPane().setContent(grid);

        // Abilita/disabilita "Conferma" se i campi sono vuoti
        Node confirm = getDialogPane().lookupButton(confirmBtn);
        confirm.setDisable(true);
        userField.textProperty().addListener((o, oldV, newV) -> {
            confirm.setDisable(newV == null || newV.isBlank() || pwdField.getText() == null || pwdField.getText().isBlank());
        });
        pwdField.textProperty().addListener((o, oldV, newV) -> {
            confirm.setDisable(newV == null || newV.isBlank() || userField.getText() == null || userField.getText().isBlank());
        });

        setResultConverter(bt -> {
            if (bt == confirmBtn) {
                Theme t = rbBN.isSelected() ? Theme.BIANCO_NERO : Theme.COLORI;
                return new StartupResult(
                        userField.getText(),
                        pwdField.getText(),
                        t
                );
            }
            return null; // Chiudi/annulla
        });
    }
}