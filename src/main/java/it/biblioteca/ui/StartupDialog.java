package it.biblioteca.ui;

import it.biblioteca.ui.ContentManager.Theme;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

public class StartupDialog extends Dialog<StartupResult> {

    private static final String DEFAULT_DB_USER = "Admin";
    private static final String DEFAULT_DB_PASS = "admin";

    private final TextField txtAppUser = new TextField();
    private final PasswordField txtAppPass = new PasswordField();

    private final TextField txtDbUser = new TextField(DEFAULT_DB_USER);
    private final PasswordField txtDbPass = new PasswordField();

    private final ToggleGroup themeGroup = new ToggleGroup();

    public StartupDialog() {
        setTitle("Accesso");
        setHeaderText("Seleziona tema e inserisci le credenziali applicative");

        txtDbPass.setText(DEFAULT_DB_PASS);

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(16));

        int r = 0;

        Label lTheme = new Label("Tema:");
        RadioButton rbColori = new RadioButton("Colori");
        rbColori.setToggleGroup(themeGroup);
        rbColori.setSelected(true);
        RadioButton rbBw = new RadioButton("Bianco/Nero");
        rbBw.setToggleGroup(themeGroup);

        form.add(lTheme, 0, r);
        form.add(new HBox(12, rbColori, rbBw), 1, r++);
        form.add(new Label("Utente app:"), 0, r);
        form.add(txtAppUser, 1, r++);
        form.add(new Label("Password app:"), 0, r);
        form.add(txtAppPass, 1, r++);

        Label lDbUser = new Label("DB User:");
        Label lDbPass = new Label("DB Pass:");
        lDbUser.setManaged(false); lDbUser.setVisible(false);
        lDbPass.setManaged(false); lDbPass.setVisible(false);
        txtDbUser.setManaged(false); txtDbUser.setVisible(false);
        txtDbPass.setManaged(false); txtDbPass.setVisible(false);

        form.add(lDbUser, 0, r);
        form.add(txtDbUser, 1, r++);
        form.add(lDbPass, 0, r);
        form.add(txtDbPass, 1, r++);

        getDialogPane().setContent(form);

        ButtonType okType = new ButtonType("Entra", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(okType, ButtonType.CANCEL);

        Button okBtn = (Button) getDialogPane().lookupButton(okType);
        okBtn.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            String au = txtAppUser.getText() == null ? "" : txtAppUser.getText().trim();
            String ap = txtAppPass.getText() == null ? "" : txtAppPass.getText().trim();
            if (au.isBlank() || ap.isBlank()) {
                Alert a = new Alert(Alert.AlertType.ERROR, "Inserisci le credenziali applicative.", ButtonType.OK);
                a.setHeaderText(null);
                a.setTitle("Errore");
                a.showAndWait();
                ev.consume();
            }
        });

        setResultConverter(bt -> {
            if (bt != okType) return null;
            Theme theme = themeGroup.getSelectedToggle() == rbBw ? Theme.BIANCO_NERO : Theme.COLORI;

            return new StartupResult(
                    txtDbUser.getText(),              // DB username (nascosto, default)
                    txtDbPass.getText(),              // DB password (nascosto, default)
                    txtAppUser.getText().trim(),      // app username
                    txtAppPass.getText().trim(),      // app password
                    theme
            );
        });
    }

    private static final class HBox extends javafx.scene.layout.HBox {
        HBox(double spacing, RadioButton a, RadioButton b) {
            super(spacing, a, b);
        }
    }
}