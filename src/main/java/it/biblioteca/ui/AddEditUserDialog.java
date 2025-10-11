package it.biblioteca.ui;

import it.biblioteca.bean.UtenteBean;
import it.biblioteca.entity.Utente;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;

public class AddEditUserDialog extends Dialog<UtenteBean> {
    private final TextField txtTessera = new TextField();
    private final TextField txtNome = new TextField();
    private final TextField txtCognome = new TextField();
    private final TextField txtEmail = new TextField();
    private final TextField txtTelefono = new TextField();
    private final DatePicker dpAttivazione = new DatePicker();
    private final DatePicker dpScadenza = new DatePicker();

    public AddEditUserDialog(Utente existing) {
        setTitle(existing == null ? "Nuovo Utente" : "Modifica Utente");
        setHeaderText(existing == null ? "Inserisci i dati dell'utente" : "Aggiorna i dati dell'utente");

        GridPane form = buildForm();
        if (existing != null) populateForm(existing); else dpAttivazione.setValue(LocalDate.now());

        getDialogPane().setContent(form);
        ButtonType okType = new ButtonType(existing == null ? "Crea" : "Salva", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(okType, ButtonType.CANCEL);

        Button okBtn = (Button) getDialogPane().lookupButton(okType);
        setupValidation(okBtn);

        setResultConverter(bt -> bt == okType ? buildBean(existing) : null);
    }

    private GridPane buildForm() {
        GridPane form = new GridPane();
        form.setHgap(10); form.setVgap(10); form.setPadding(new Insets(15));
        int r = 0;
        form.add(new Label("Tessera:"), 0, r); form.add(txtTessera, 1, r++);
        form.add(new Label("Nome:"), 0, r); form.add(txtNome, 1, r++);
        form.add(new Label("Cognome:"), 0, r); form.add(txtCognome, 1, r++);
        form.add(new Label("Email:"), 0, r); form.add(txtEmail, 1, r++);
        form.add(new Label("Telefono:"), 0, r); form.add(txtTelefono, 1, r++);
        form.add(new Label("Data attivazione:"), 0, r); form.add(dpAttivazione, 1, r++);
        form.add(new Label("Data scadenza:"), 0, r); form.add(dpScadenza, 1, r++);
        return form;
    }

    private void populateForm(Utente existing) {
        txtTessera.setText(existing.getTessera() != null ? String.valueOf(existing.getTessera()) : "");
        txtNome.setText(existing.getNome());
        txtCognome.setText(existing.getCognome());
        txtEmail.setText(existing.getEmail());
        txtTelefono.setText(existing.getTelefono());
        dpAttivazione.setValue(existing.getDataAttivazione());
        dpScadenza.setValue(existing.getDataScadenza());
    }

    private void setupValidation(Button okBtn) {
        okBtn.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            if (!isValidForm()) ev.consume();
        });
    }

    private boolean isValidForm() {
        String tesseraStr = txtTessera.getText().trim();
        if (tesseraStr.isEmpty()) { showError("La tessera è obbligatoria."); return false; }
        try { Integer.parseInt(tesseraStr); }
        catch (NumberFormatException _) { showError("La tessera deve essere numerica."); return false; }
        LocalDate att = dpAttivazione.getValue();
        LocalDate scad = dpScadenza.getValue();
        if (att != null && scad != null && scad.isBefore(att)) {
            showError("La data di scadenza non può essere precedente all'attivazione.");
            return false;
        }
        return true;
    }

    private UtenteBean buildBean(Utente existing) {
        UtenteBean b = new UtenteBean();
        if (existing != null) b.setId(existing.getId());
        b.setTessera(Integer.parseInt(txtTessera.getText().trim()));
        b.setNome(txtNome.getText().trim());
        b.setCognome(txtCognome.getText().trim());
        b.setEmail(txtEmail.getText().trim());
        b.setTelefono(txtTelefono.getText().trim());
        b.setDataAttivazione(dpAttivazione.getValue());
        b.setDataScadenza(dpScadenza.getValue());
        return b;
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }
}