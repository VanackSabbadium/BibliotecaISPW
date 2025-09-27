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

        // Form
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(15));

        int r = 0;
        TextField txtLibro = new TextField();
        txtLibro.setEditable(false);
        TextField txtUtente = new TextField();
        txtUtente.setEditable(false);

        String titoloLibro = selectedBook != null && selectedBook.getTitolo() != null ? selectedBook.getTitolo() : "";
        String nome = selectedUser != null && selectedUser.getNome() != null ? selectedUser.getNome() : "";
        String cognome = selectedUser != null && selectedUser.getCognome() != null ? selectedUser.getCognome() : "";
        Integer tessera = selectedUser != null ? selectedUser.getTessera() : null;
        String utenteDisplay = (nome + " " + cognome).trim() + (tessera != null ? " (Tessera: " + tessera + ")" : "");

        txtLibro.setText(titoloLibro);
        txtUtente.setText(utenteDisplay);
        dpDataPrestito.setPromptText("Data prestito");

        form.add(new Label("Libro:"), 0, r);
        form.add(txtLibro, 1, r++);
        form.add(new Label("Utente:"), 0, r);
        form.add(txtUtente, 1, r++);
        form.add(new Label("Data prestito:"), 0, r);
        form.add(dpDataPrestito, 1, r++);

        getDialogPane().setContent(form);

        ButtonType okType = new ButtonType("Conferma", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(okType, ButtonType.CANCEL);

        // Validazione minima su conferma
        Button okBtn = (Button) getDialogPane().lookupButton(okType);
        okBtn.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            if (dpDataPrestito.getValue() == null) {
                showError();
                ev.consume();
            }
        });

        setResultConverter(bt -> {
            if (bt != okType) return null;

            PrestitoBean bean = new PrestitoBean();
            // Riferimenti
            bean.setLibroId(selectedBook != null ? selectedBook.getId() : null);
            bean.setUtenteId(selectedUser != null ? selectedUser.getId() : null);

            // Snapshot
            bean.setLibroTitoloSnapshot(titoloLibro);
            bean.setUtenteSnapshot((nome + " " + cognome).trim());

            // Data
            LocalDate dp = dpDataPrestito.getValue() != null ? dpDataPrestito.getValue() : LocalDate.now();
            bean.setDataPrestito(dp);

            return bean;
        });
    }

    private void showError() {
        Alert a = new Alert(Alert.AlertType.ERROR, "La data del prestito è obbligatoria.", ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }
}