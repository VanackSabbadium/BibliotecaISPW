// src/main/java/it/biblioteca/ui/UsersWindow.java
package it.biblioteca.ui;

import it.biblioteca.controller.UtenteController;
import it.biblioteca.entity.Utente;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class UsersWindow {
    private final UtenteController controller;
    private final Stage stage;
    private final TableView<Utente> table = new TableView<>();
    private final ObservableList<Utente> data = FXCollections.observableArrayList();

    public UsersWindow(UtenteController controller) {
        this.controller = controller;
        this.stage = new Stage();
        stage.setTitle("Gestione Utenti");
        stage.initModality(Modality.NONE);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        Button btnAdd = new Button("Nuovo");
        Button btnEdit = new Button("Modifica");
        Button btnDelete = new Button("Elimina");
        HBox toolbar = new HBox(10, btnAdd, btnEdit, btnDelete);
        toolbar.setPadding(new Insets(0, 0, 10, 0));
        root.setTop(toolbar);

        TableColumn<Utente, Integer> tesseraCol = new TableColumn<>("Tessera");
        tesseraCol.setCellValueFactory(new PropertyValueFactory<>("tessera"));
        TableColumn<Utente, String> nomeCol = new TableColumn<>("Nome");
        nomeCol.setCellValueFactory(new PropertyValueFactory<>("nome"));
        TableColumn<Utente, String> cognomeCol = new TableColumn<>("Cognome");
        cognomeCol.setCellValueFactory(new PropertyValueFactory<>("cognome"));
        TableColumn<Utente, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        TableColumn<Utente, String> telCol = new TableColumn<>("Telefono");
        telCol.setCellValueFactory(new PropertyValueFactory<>("telefono"));

        table.getColumns().addAll(tesseraCol, nomeCol, cognomeCol, emailCol, telCol);
        table.setItems(data);
        table.setPlaceholder(new Label("Nessun utente"));

        root.setCenter(table);

        btnAdd.setOnAction(e -> {
            AddEditUserDialog dlg = new AddEditUserDialog(controller, null);
            dlg.showAndWait().ifPresent(bean -> {
                if (controller.aggiungi(bean)) refresh();
                else showError("Impossibile aggiungere l'utente (tessera duplicata?)");
            });
        });

        btnEdit.setOnAction(e -> {
            Utente sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { showError("Seleziona un utente da modificare."); return; }
            AddEditUserDialog dlg = new AddEditUserDialog(controller, sel);
            dlg.showAndWait().ifPresent(bean -> {
                bean.setId(sel.getId());
                if (controller.aggiorna(bean)) refresh();
                else showError("Impossibile aggiornare l'utente.");
            });
        });

        btnDelete.setOnAction(e -> {
            Utente sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { showError("Seleziona un utente da eliminare."); return; }
            if (controller.elimina(sel.getId())) refresh();
            else showError("Impossibile eliminare l'utente.");
        });

        refresh();
        stage.setScene(new Scene(root, 700, 450));
    }

    public void show() { stage.show(); }

    private void refresh() {
        data.setAll(controller.trovaTutti());
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }
}