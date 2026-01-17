package it.biblioteca;

import javafx.application.Application;

public class Main {

    private Main() {
        throw new IllegalStateException("Utility class");
    }

    static void main(String[] args) {
        Application.launch(BibliotecaApp.class, args);
    }
}