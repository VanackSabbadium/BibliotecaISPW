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

/**
 * Dialog per creare/modificare un Utente.
 * Valida i campi con ValidationUtils:
 *  - Tessera: obbligatoria, numerica
 *  - Email: se presente, formato valido
 *  - Data scadenza >= Data attivazione (se entrambe presenti)
 */
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

        getDialogPane().setContent(form);
        ButtonType okType = new ButtonType(existing == null ? "Crea" : "Salva", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(okType, ButtonType.CANCEL);

        // Validazioni centralizzate con ValidationUtils
        Button okBtn = (Button) getDialogPane().lookupButton(okType);
        okBtn.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            List<String> errors = new ArrayList<>();

            // Tessera: obbligatoria + numerica
            Integer tessera = ValidationUtils.parseInteger(txtTessera.getText(), "Tessera", true, errors);

            // Email: se presente, formato valido
            ValidationUtils.validateEmailIfPresent(txtEmail.getText(), errors);

            // Date: scadenza >= attivazione (se entrambe presenti)
            LocalDate att = dpAttivazione.getValue();
            LocalDate scad = dpScadenza.getValue();
            ValidationUtils.validateDateOrder(att, scad, "Data attivazione", "Data scadenza", errors);

            if (!errors.isEmpty()) {
                showError(String.join("\n", errors));
                ev.consume();
            }

            // Se tutto ok, tessera verrà usata nel resultConverter
        });

        setResultConverter(bt -> {
            if (bt != okType) return null;
            UtenteBean b = new UtenteBean();
            if (existing != null) b.setId(existing.getId());

            // tessera è validata in precedenza
            b.setTessera(Integer.parseInt(txtTessera.getText().trim()));
            b.setNome(txtNome.getText() != null ? txtNome.getText().trim() : "");
            b.setCognome(txtCognome.getText() != null ? txtCognome.getText().trim() : "");
            b.setEmail(txtEmail.getText() != null ? txtEmail.getText().trim() : "");
            b.setTelefono(txtTelefono.getText() != null ? txtTelefono.getText().trim() : "");
            b.setDataAttivazione(dpAttivazione.getValue());
            b.setDataScadenza(dpScadenza.getValue());
            return b;
        });
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.setTitle("Errore di validazione");
        a.showAndWait();
    }

    private static String nullToEmpty(String s) { return s == null ? "" : s; }
}