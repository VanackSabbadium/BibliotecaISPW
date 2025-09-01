module it.biblioteca {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql; // per JDBC nei DAO

    // Esporta il package principale (se serve altrove)
    exports it.biblioteca;

    // Apre i package usati da FXML/Controller per la riflessione di JavaFX
    opens it.biblioteca to javafx.fxml;
    opens it.biblioteca.controller to javafx.fxml;
    // Se hai FXML anche in altri package (es. ui), aprili:
    // opens it.biblioteca.ui to javafx.fxml;
}