package it.biblioteca.ui;

import it.biblioteca.entity.Book;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.List;

public class SelectBookDialog extends Dialog<Book> {
    private final TableView<Book> table = new TableView<>();

    public SelectBookDialog(List<Book> books) {
        setTitle("Seleziona Libro");
        setHeaderText("Scegli un libro dal catalogo");

        TableColumn<Book, String> isbnCol = new TableColumn<>("ISBN");
        isbnCol.setCellValueFactory(new PropertyValueFactory<>("isbn"));

        TableColumn<Book, String> titoloCol = new TableColumn<>("Titolo");
        titoloCol.setCellValueFactory(new PropertyValueFactory<>("titolo"));

        TableColumn<Book, String> autoreCol = new TableColumn<>("Autore");
        autoreCol.setCellValueFactory(new PropertyValueFactory<>("autore"));

        TableColumn<Book, LocalDate> dataCol = new TableColumn<>("Data Pubblicazione");
        dataCol.setCellValueFactory(new PropertyValueFactory<>("dataPubblicazione"));

        table.getColumns().addAll(isbnCol, titoloCol, autoreCol, dataCol);
        table.getItems().setAll(books);
        table.setPrefHeight(320);

        VBox content = new VBox(10, new Label("Catalogo disponibile:"), table);
        content.setPadding(new Insets(15));
        getDialogPane().setContent(content);

        ButtonType okType = new ButtonType("Seleziona", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(okType, ButtonType.CANCEL);

        getDialogPane().lookupButton(okType).disableProperty().bind(
                table.getSelectionModel().selectedItemProperty().isNull()
        );

        setResultConverter(btn -> {
            if (btn == okType) {
                return table.getSelectionModel().getSelectedItem();
            }
            return null;
        });
    }
}
