package it.biblioteca.ui;

import it.biblioteca.bean.BookBean;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;

public class AddBookDialog extends Dialog<BookBean> {
    private final TextField txtIsbn = new TextField();
    private final TextField txtTitolo = new TextField();
    private final TextField txtAutore = new TextField();
    private final DatePicker dpDataPub = new DatePicker(LocalDate.now());
    private final TextField txtCasa = new TextField();
    private final Spinner<Integer> spCopie = new Spinner<>(1, 999, 1);

    public AddBookDialog() {
        setTitle("Aggiungi Libro");
        setHeaderText("Inserisci i dati del libro");

        GridPane g = new GridPane();
        g.setHgap(10); g.setVgap(10); g.setPadding(new Insets(20));
        g.addRow(0, new Label("ISBN:"), txtIsbn);
        g.addRow(1, new Label("Titolo:"), txtTitolo);
        g.addRow(2, new Label("Autore:"), txtAutore);
        g.addRow(3, new Label("Data Pubblicazione:"), dpDataPub);
        g.addRow(4, new Label("Casa Editrice:"), txtCasa);
        g.addRow(5, new Label("Copie:"), spCopie);

        getDialogPane().setContent(g);
        ButtonType salva = new ButtonType("Salva", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(salva, ButtonType.CANCEL);

        getDialogPane().lookupButton(salva).disableProperty().bind(
                txtIsbn.textProperty().isEmpty()
                        .or(txtTitolo.textProperty().isEmpty())
                        .or(txtAutore.textProperty().isEmpty())
                        .or(dpDataPub.valueProperty().isNull())
                        .or(txtCasa.textProperty().isEmpty())
        );

        setResultConverter(bt -> {
            if (bt == salva) {
                BookBean b = new BookBean();
                b.setIsbn(txtIsbn.getText().trim());
                b.setTitolo(txtTitolo.getText().trim());
                b.setAutore(txtAutore.getText().trim());
                b.setDataPubblicazione(dpDataPub.getValue());
                b.setCasaEditrice(txtCasa.getText().trim());
                b.setCopie(spCopie.getValue());
                return b;
            }
            return null;
        });
    }
}