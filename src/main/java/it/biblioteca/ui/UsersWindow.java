package it.biblioteca.ui;

import it.biblioteca.controller.UtenteController;
import it.biblioteca.entity.Utente;
import it.biblioteca.security.CredentialsService;
import it.biblioteca.security.SessionContext;
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

/**
 * Finestra/Tab per gestione utenti.
 * Nota: se vuoi che sia una tab nella stessa finestra principale, dovrai adattare il codice del ContentManager
 * per costruire un Tab invece di creare una nuova Stage. Qui fornisco comunque la UI completa.
 */
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
        Button btnCred = new Button("Credenziali");
        HBox toolbar = new HBox(10, btnAdd, btnEdit, btnDelete, btnCred);
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

        // CREAZIONE / GESTIONE CREDENZIALI da parte di Admin
        btnCred.setOnAction(e -> {
            if (!SessionContext.isAdmin()) { showError("Solo Admin può gestire le credenziali."); return; }
            Utente sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { showError("Seleziona un utente per gestire le credenziali."); return; }

            // Carica eventuali credenziali esistenti
            CredentialsService.CredInfo ci = CredentialsService.findByUtenteId(sel.getId());
            TextInputDialog userDlg = new TextInputDialog(ci != null ? ci.username : "");
            userDlg.setTitle("Crea/Aggiorna Credenziali");
            userDlg.setHeaderText("Inserisci username per l'utente: " + sel.getNome() + " " + sel.getCognome());
            userDlg.setContentText("Username:");
            userDlg.showAndWait().ifPresent(un -> {
                TextInputDialog passDlg = new TextInputDialog();
                passDlg.setTitle("Password");
                passDlg.setHeaderText("Inserisci la password (in chiaro, verrà hashata):");
                passDlg.setContentText("Password:");
                passDlg.showAndWait().ifPresent(pw -> {
                    // se ci sono già credenziali -> update, altrimenti insert
                    boolean ok;
                    if (ci == null) {
                        ok = CredentialsService.creaCredenziali(sel.getId(), un, pw, "UTENTE");
                    } else {
                        ok = CredentialsService.aggiornaCredenziali(sel.getId(), un, pw, "UTENTE");
                    }
                    if (ok) {
                        showInfo("Credenziali salvate.");
                        refresh();
                    } else {
                        showError("Impossibile salvare le credenziali. Verifica che username sia unico e che il DB sia accessibile.");
                    }
                });
            });

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

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
