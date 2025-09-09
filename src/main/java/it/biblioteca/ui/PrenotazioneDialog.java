package it.biblioteca.ui;

import it.biblioteca.bean.PrenotazioneBean;
import it.biblioteca.entity.Book;
import it.biblioteca.entity.Utente;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;

/**
 * Dialog per confermare una prenotazione.
 * Mostra titolo libro e utente, data prenotazione (default oggi).
 */
public class PrenotazioneDialog extends Dialog<PrenotazioneBean> {
    private final Book book;
    private final Utente user;
    private final TextField txtLibro = new TextField();
    private final TextField txtUtente = new TextField();
    private final DatePicker dpData = new DatePicker(LocalDate.now());

    public PrenotazioneDialog(Book book, Utente user) {
        this.book = book;
        this.user = user;

        setTitle("Conferma Prenotazione");
        setHeaderText("Verifica i dati e conferma la prenotazione");

        GridPane g = new GridPane();
        g.setHgap(10);
        g.setVgap(10);
        g.setPadding(new Insets(10));

        txtLibro.setEditable(false);
        txtUtente.setEditable(false);

        String titolo = book != null ? book.getTitolo() : "";
        String nome = user != null ? user.getNome() : "";
        String cognome = user != null ? user.getCognome() : "";
        String utenteDisplay = (nome + " " + cognome).trim() + (user != null && user.getTessera() != null ? " (Tessera: " + user.getTessera() + ")" : "");

        txtLibro.setText(titolo);
        txtUtente.setText(utenteDisplay);

        int r = 0;
        g.addRow(r++, new Label("Libro:"), txtLibro);
        g.addRow(r++, new Label("Utente:"), txtUtente);
        g.addRow(r++, new Label("Data prenotazione:"), dpData);

        getDialogPane().setContent(g);

        ButtonType ok = new ButtonType("Prenota", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        setResultConverter(bt -> {
            if (bt != ok) return null;
            PrenotazioneBean b = new PrenotazioneBean();
            if (book != null) b.setLibroId(book.getId());
            if (user != null) b.setUtenteId(user.getId());
            b.setDataPrenotazione(dpData.getValue() != null ? dpData.getValue() : LocalDate.now());
            b.setLibroTitoloSnapshot(titolo);
            b.setUtenteSnapshot((nome + " " + cognome).trim());
            return b;
        });
    }
}