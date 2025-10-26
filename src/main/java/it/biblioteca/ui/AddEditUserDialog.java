package it.biblioteca.ui;

import it.biblioteca.bean.UtenteBean;
import it.biblioteca.entity.Utente;
import it.biblioteca.validation.ValidationUtils;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AddEditUserDialog extends Dialog<UtenteBean> {

    private static final String LABEL_DATA_ATTIVAZIONE = "Data attivazione";
    private static final String LABEL_DATA_SCADENZA   = "Data scadenza";

    private final TextField txtTessera = new TextField();
    private final TextField txtNome = new TextField();
    private final TextField txtCognome = new TextField();
    private final TextField txtEmail = new TextField();
    private final TextField txtTelefono = new TextField();
    private final DatePicker dpAttivazione = new DatePicker();
    private final DatePicker dpScadenza = new DatePicker();

    public AddEditUserDialog(Utente existing) {
        setTitle(existing == null ? "Nuovo Utente" : "Modifica Utente");
        setHeaderText(existing == null ? "Inserisci i dati del nuovo utente" : "Aggiorna i dati dell'utente");

        GridPane form = buildForm();
        getDialogPane().setContent(form);

        ButtonType okType = new ButtonType(existing == null ? "Crea" : "Salva", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(okType, ButtonType.CANCEL);

        initializeFields(existing);
        attachValidation(okType);
        configureResultConverter(okType, existing);
    }

    private GridPane buildForm() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));

        txtTessera.setPromptText("Numero tessera (obbligatorio)");
        txtNome.setPromptText("Nome");
        txtCognome.setPromptText("Cognome");
        txtEmail.setPromptText("email@esempio.com");
        txtTelefono.setPromptText("Telefono");
        dpAttivazione.setPromptText(LABEL_DATA_ATTIVAZIONE);
        dpScadenza.setPromptText(LABEL_DATA_SCADENZA);

        int r = 0;
        grid.add(new Label("Tessera"), 0, r); grid.add(txtTessera, 1, r++);
        grid.add(new Label("Nome"), 0, r); grid.add(txtNome, 1, r++);
        grid.add(new Label("Cognome"), 0, r); grid.add(txtCognome, 1, r++);
        grid.add(new Label("Email"), 0, r); grid.add(txtEmail, 1, r++);
        grid.add(new Label("Telefono"), 0, r); grid.add(txtTelefono, 1, r++);
        grid.add(new Label(LABEL_DATA_ATTIVAZIONE), 0, r); grid.add(dpAttivazione, 1, r++);
        grid.add(new Label(LABEL_DATA_SCADENZA), 0, r); grid.add(dpScadenza, 1, r);

        return grid;
    }

    private void initializeFields(Utente existing) {
        if (existing != null) {
            txtTessera.setText(existing.getTessera() != null ? String.valueOf(existing.getTessera()) : "");
            txtNome.setText(nullToEmpty(existing.getNome()));
            txtCognome.setText(nullToEmpty(existing.getCognome()));
            txtEmail.setText(nullToEmpty(existing.getEmail()));
            txtTelefono.setText(nullToEmpty(existing.getTelefono()));
            dpAttivazione.setValue(existing.getDataAttivazione());
            dpScadenza.setValue(existing.getDataScadenza());
        } else {
            dpAttivazione.setValue(LocalDate.now());
        }
    }

    private void attachValidation(ButtonType okType) {
        Button okBtn = (Button) getDialogPane().lookupButton(okType);
        okBtn.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            List<String> errors = validateForm();
            if (!errors.isEmpty()) {
                showError(String.join("\n", errors));
                ev.consume();
            }
        });
    }

    private void configureResultConverter(ButtonType okType, Utente existing) {
        setResultConverter(bt -> {
            if (bt != okType) return null;
            UtenteBean b = new UtenteBean();
            if (existing != null) b.setId(existing.getId());
            populateBeanFromFields(b);
            return b;
        });
    }

    private List<String> validateForm() {
        List<String> errors = new ArrayList<>();

        ValidationUtils.parseInteger(txtTessera.getText(), "Tessera", true, errors);

        ValidationUtils.validateEmailIfPresent(txtEmail.getText(), errors);

        LocalDate att = dpAttivazione.getValue();
        LocalDate scad = dpScadenza.getValue();
        ValidationUtils.validateDateOrder(att, scad, LABEL_DATA_ATTIVAZIONE, LABEL_DATA_SCADENZA, errors);

        return errors;
    }

    private void populateBeanFromFields(UtenteBean b) {
        String tess = txtTessera.getText() != null ? txtTessera.getText().trim() : "";
        b.setTessera(tess.isEmpty() ? null : Integer.parseInt(tess));
        b.setNome(safeTrim(txtNome.getText()));
        b.setCognome(safeTrim(txtCognome.getText()));
        b.setEmail(safeTrim(txtEmail.getText()));
        b.setTelefono(safeTrim(txtTelefono.getText()));
        b.setDataAttivazione(dpAttivazione.getValue());
        b.setDataScadenza(dpScadenza.getValue());
    }

    private static String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.setTitle("Errore di validazione");
        a.showAndWait();
    }

    private static String nullToEmpty(String s) { return s == null ? "" : s; }
}