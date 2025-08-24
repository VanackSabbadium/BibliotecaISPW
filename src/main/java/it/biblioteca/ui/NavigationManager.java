// src/main/java/it/biblioteca/ui/NavigationManager.java
package it.biblioteca.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class NavigationManager {
    private Button btnHome;
    private Button btnBooks;
    private Button btnLoans;
    private Button btnUsers;

    public void inizializzaNavigazione(BorderPane root) {
        VBox nav = new VBox(10);
        nav.setPadding(new Insets(10));
        btnHome = new Button("Home");
        btnBooks = new Button("Catalogo");
        btnLoans = new Button("Prestiti");
        btnUsers = new Button("Utenti");

        nav.getChildren().addAll(btnHome, btnBooks, btnLoans, btnUsers);
        root.setLeft(nav);
    }

    public void setHomeButtonAction(javafx.event.EventHandler<javafx.event.ActionEvent> h) { btnHome.setOnAction(h); }
    public void setBookButtonAction(javafx.event.EventHandler<javafx.event.ActionEvent> h) { btnBooks.setOnAction(h); }
    public void setLoanButtonAction(javafx.event.EventHandler<javafx.event.ActionEvent> h) { btnLoans.setOnAction(h); }
    public void setUserButtonAction(javafx.event.EventHandler<javafx.event.ActionEvent> h) { btnUsers.setOnAction(h); }
}