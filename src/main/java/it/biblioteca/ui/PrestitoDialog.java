package it.biblioteca.ui;

import it.biblioteca.bean.PrestitoBean;
import it.biblioteca.entity.Book;
import it.biblioteca.entity.Utente;
import it.biblioteca.validation.ValidationUtils;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog di conferma prestito.
 * Valida con ValidationUtils che la Data Prestito sia presente.
 */
public class PrestitoDialog extends Dialog<PrestitoBean> {

    private static final String LABEL_DATA_PRESTITO = "Data prestito";

    private final DatePicker dpDataPrestito = new DatePicker();
    private final TextField txtLibro = nonEditableField();
    private final TextField txtUtente = nonEditableField();

    public PrestitoDialog(Book selectedBook, Utente selectedUser) {
        setTitle("Conferma Prestito");
        setHeaderText("Verifica i dati e conferma il prestito");

        GridPane form = buildForm();
        initializeFields(selectedBook, selectedUser);
        getDialogPane().setContent(form);

        ButtonType okType = new ButtonType("Conferma", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(okType, ButtonType.CANCEL);

        attachValidation(okType);
        configureResultConverter(okType, selectedBook, selectedUser);
    }

    /* =========================
       Metodi di supporto
       ========================= */

    private GridPane buildForm() {
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(15));

        dpDataPrestito.setPromptText(LABEL_DATA_PRESTITO);

        int r = 0;
        form.add(new Label("Libro:"), 0, r);
        form.add(txtLibro, 1, r++);
        form.add(new Label("Utente:"), 0, r);
        form.add(txtUtente, 1, r++);
        form.add(new Label(LABEL_DATA_PRESTITO + ":"), 0, r);
        form.add(dpDataPrestito, 1, r++);

        return form;
    }

    private void initializeFields(Book selectedBook, Utente selectedUser) {
        txtLibro.setText(getBookTitle(selectedBook));
        txtUtente.setText(buildUserDisplay(selectedUser));
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

    private List<String> validateForm() {
        List<String> errors = new ArrayList<>();
        ValidationUtils.requireNotNull(dpDataPrestito.getValue(), LABEL_DATA_PRESTITO, errors);
        return errors;
    }

    private void configureResultConverter(ButtonType okType, Book selectedBook, Utente selectedUser) {
        setResultConverter(bt -> {
            if (bt != okType) return null;

            PrestitoBean bean = new PrestitoBean();
            bean.setLibroId(selectedBook != null ? selectedBook.getId() : null);
            bean.setUtenteId(selectedUser != null ? selectedUser.getId() : null);

            bean.setLibroTitoloSnapshot(getBookTitle(selectedBook));
            bean.setUtenteSnapshot(buildUserSnapshot(selectedUser));

            LocalDate dp = dpDataPrestito.getValue() != null ? dpDataPrestito.getValue() : LocalDate.now();
            bean.setDataPrestito(dp);

            return bean;
        });
    }

    private static TextField nonEditableField() {
        TextField tf = new TextField();
        tf.setEditable(false);
        return tf;
    }

    private static String getBookTitle(Book b) {
        return b != null && b.getTitolo() != null ? b.getTitolo() : "";
    }

    private static String buildUserDisplay(Utente u) {
        if (u == null) return "";
        String nome = u.getNome() != null ? u.getNome() : "";
        String cognome = u.getCognome() != null ? u.getCognome() : "";
        Integer tessera = u.getTessera();
        String base = (nome + " " + cognome).trim();
        return tessera != null ? base + " (Tessera: " + tessera + ")" : base;
    }

    private static String buildUserSnapshot(Utente u) {
        if (u == null) return "";
        String nome = u.getNome() != null ? u.getNome() : "";
        String cognome = u.getCognome() != null ? u.getCognome() : "";
        return (nome + " " + cognome).trim();
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.setTitle("Errore di validazione");
        a.showAndWait();
    }
}