package it.biblioteca.ui;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

/**
 * Dialog semplice per creare/modificare username+password per un utente.
 * Restituisce Pair<username,password> se OK, altrimenti empty.
 */
public class CredentialsDialog extends Dialog<Pair<String, String>> {

    public CredentialsDialog(Long userId, String existingUsername, String existingPasswordPlain) {
        setTitle("Crea/Modifica credenziali");
        setHeaderText("Inserisci username e password per l'utente selezionato");

        ButtonType okType = new ButtonType("Salva", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(okType, ButtonType.CANCEL);

        GridPane g = new GridPane();
        g.setHgap(8);
        g.setVgap(8);
        g.setPadding(new Insets(10));

        TextField txtUser = new TextField();
        txtUser.setPromptText("username");
        if (existingUsername != null) txtUser.setText(existingUsername);

        PasswordField txtPass = new PasswordField();
        txtPass.setPromptText("password");
        if (existingPasswordPlain != null) txtPass.setText(existingPasswordPlain);

        g.add(new Label("Username:"), 0, 0);
        g.add(txtUser, 1, 0);
        g.add(new Label("Password:"), 0, 1);
        g.add(txtPass, 1, 1);

        getDialogPane().setContent(g);

        Node okBtn = getDialogPane().lookupButton(okType);
        okBtn.setDisable(true);

        Runnable validate = () -> {
            String u = txtUser.getText();
            String p = txtPass.getText();
            okBtn.setDisable(u == null || u.isBlank() || p == null || p.isBlank());
        };
        txtUser.textProperty().addListener((o,a,b) -> validate.run());
        txtPass.textProperty().addListener((o,a,b) -> validate.run());
        validate.run();

        setResultConverter(bt -> {
            if (bt == okType) {
                return new Pair<>(txtUser.getText().trim(), txtPass.getText());
            }
            return null;
        });
    }
}
