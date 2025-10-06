package it.biblioteca.ui;

import it.biblioteca.bean.PrestitoBean;
import it.biblioteca.entity.Book;
import it.biblioteca.entity.Utente;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;

public class PrestitoDialog extends Dialog<PrestitoBean> {

    private final DatePicker dpDataPrestito = new DatePicker(LocalDate.now());

    public PrestitoDialog(Book selectedBook, Utente selectedUser) {
        setTitle("Conferma Prestito");
        setHeaderText("Verifica i dati e conferma il prestito");

        String titoloLibro = safeTitle(selectedBook);
        String utenteDisplay = displayUser(selectedUser);
        String utenteSnapshot = safeName(selectedUser);

        GridPane form = buildForm(titoloLibro, utenteDisplay);
        getDialogPane().setContent(form);

        ButtonType okType = new ButtonType("Conferma", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(okType, ButtonType.CANCEL);

        Button okBtn = (Button) getDialogPane().lookupButton(okType);
        setupValidation(okBtn);

        setResultConverter(bt -> bt == okType ? buildBean(selectedBook, selectedUser, titoloLibro, utenteSnapshot) : null);
    }

    private GridPane buildForm(String titoloLibro, String utenteDisplay) {
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(15));

        TextField txtLibro = new TextField(titoloLibro);
        txtLibro.setEditable(false);
        TextField txtUtente = new TextField(utenteDisplay);
        txtUtente.setEditable(false);
        dpDataPrestito.setPromptText("Data prestito");

        int r = 0;
        form.add(new Label("Libro:"), 0, r);
        form.add(txtLibro, 1, r++);
        form.add(new Label("Utente:"), 0, r);
        form.add(txtUtente, 1, r++);
        form.add(new Label("Data prestito:"), 0, r);
        form.add(dpDataPrestito, 1, r++);

        return form;
    }

    private void setupValidation(Button okBtn) {
        okBtn.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            if (dpDataPrestito.getValue() == null) {
                showError();
                ev.consume();
            }
        });
    }

    private PrestitoBean buildBean(Book b, Utente u, String titoloLibro, String utenteSnapshot) {
        PrestitoBean bean = new PrestitoBean();
        bean.setLibroId(b != null ? b.getId() : null);
        bean.setUtenteId(u != null ? u.getId() : null);
        bean.setLibroTitoloSnapshot(titoloLibro);
        bean.setUtenteSnapshot(utenteSnapshot);
        LocalDate dp = dpDataPrestito.getValue() != null ? dpDataPrestito.getValue() : LocalDate.now();
        bean.setDataPrestito(dp);
        return bean;
    }

    private static String safeTitle(Book b) {
        return b != null && b.getTitolo() != null ? b.getTitolo() : "";
    }

    private static String safe(String s) {
        return s != null ? s : "";
    }

    private static String safeName(Utente u) {
        if (u == null) return "";
        String nome = safe(u.getNome());
        String cognome = safe(u.getCognome());
        return (nome + " " + cognome).trim();
    }

    private static String displayUser(Utente u) {
        if (u == null) return "";
        String base = safeName(u);
        Integer tess = u.getTessera();
        return tess != null ? base + " (Tessera: " + tess + ")" : base;
    }


    private void showError() {
        Alert a = new Alert(Alert.AlertType.ERROR, "La data del prestito è obbligatoria.", ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }
}