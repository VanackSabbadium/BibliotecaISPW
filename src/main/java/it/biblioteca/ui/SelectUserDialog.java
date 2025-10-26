package it.biblioteca.ui;

import it.biblioteca.controller.UtenteController;
import it.biblioteca.entity.Utente;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class SelectUserDialog extends Dialog<Utente> {
    private final TableView<Utente> table = new TableView<>();

    public SelectUserDialog(UtenteController utenteController, Runnable onUsersChanged) {
        setTitle("Seleziona Utente");
        setHeaderText("Scegli un utente attivo oppure creane uno nuovo");

        TableColumn<Utente, Integer> tesseraCol = new TableColumn<>("Tessera");
        tesseraCol.setCellValueFactory(new PropertyValueFactory<>("tessera"));
        TableColumn<Utente, String> nomeCol = new TableColumn<>("Nome");
        nomeCol.setCellValueFactory(new PropertyValueFactory<>("nome"));
        TableColumn<Utente, String> cognomeCol = new TableColumn<>("Cognome");
        cognomeCol.setCellValueFactory(new PropertyValueFactory<>("cognome"));
        TableColumn<Utente, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        table.getColumns().addAll(tesseraCol, nomeCol, cognomeCol, emailCol);
        table.setPlaceholder(new Label("Nessun utente attivo disponibile"));
        table.getItems().setAll(utenteController.trovaAttivi());
        table.setPrefHeight(320);

        Button btnNuovo = getButton(utenteController, onUsersChanged);

        BorderPane root = new BorderPane();
        root.setCenter(table);
        HBox bottom = new HBox(10, btnNuovo);
        bottom.setPadding(new Insets(10));
        root.setBottom(bottom);

        getDialogPane().setContent(root);

        ButtonType ok = new ButtonType("Seleziona", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);
        getDialogPane().lookupButton(ok).disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());

        setResultConverter(bt -> bt == ok ? table.getSelectionModel().getSelectedItem() : null);
    }

    private Button getButton(UtenteController utenteController, Runnable onUsersChanged) {
        Button btnNuovo = new Button("Nuovo Utente");
        btnNuovo.setOnAction(e -> {
            AddEditUserDialog dlg = new AddEditUserDialog(null);
            dlg.showAndWait().ifPresent(bean -> {
                if (utenteController.aggiungi(bean)) {
                    table.getItems().setAll(utenteController.trovaAttivi());
                    if (onUsersChanged != null) onUsersChanged.run();
                } else {
                    Alert a = new Alert(Alert.AlertType.ERROR, "Impossibile aggiungere l'utente (dati non validi o duplicati)", ButtonType.OK);
                    a.setHeaderText(null);
                    a.showAndWait();
                }
            });
        });
        return btnNuovo;
    }
}