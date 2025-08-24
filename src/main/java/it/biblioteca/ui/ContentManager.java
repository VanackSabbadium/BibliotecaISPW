package it.biblioteca.ui;

import it.biblioteca.controller.BookController;
import it.biblioteca.controller.PrestitoController;
import it.biblioteca.controller.UtenteController;
import it.biblioteca.db.DatabaseConfig;
import it.biblioteca.entity.Book;
import it.biblioteca.entity.Prestito;
import it.biblioteca.entity.Utente;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ContentManager {
    private final BookController bookController;
    private final PrestitoController prestitoController;
    private final UtenteController utenteController;

    private BorderPane rootContainer;

    public enum Theme { COLORI, BIANCO_NERO }

    private static final String THEME_COLORI_CSS = "/css/theme-color.css";
    private static final String THEME_BW_CSS     = "/css/theme-bw.css";
    private static final String THEME_COLORI_CLASS = "theme-color";
    private static final String THEME_BW_CLASS     = "theme-bw";

    private Theme currentTheme = Theme.COLORI;

    private TabPane tabPane;
    private Tab homeTab;
    private Tab catalogTab;
    private Tab loansTab;
    private Tab usersTab;

    // Catalogo
    private TableView<Book> catalogTable;
    private ObservableList<Book> catalogData;
    private FilteredList<Book> catalogFiltered;
    private SortedList<Book> catalogSorted;
    private TextField txtSearchCatalog;
    private BorderPane catalogRoot;

    // Prestiti
    private TableView<Prestito> loansTable;
    private ObservableList<Prestito> loansData;
    private FilteredList<Prestito> loansFiltered;
    private SortedList<Prestito> loansSorted;
    private TextField txtSearchLoans;
    private ComboBox<String> cmbLoanFilter;
    private BorderPane loansRoot;

    // Utenti
    private TableView<Utente> usersTable;
    private ObservableList<Utente> usersData;
    private FilteredList<Utente> usersFiltered;
    private SortedList<Utente> usersSorted;
    private TextField txtSearchUsers;
    private ComboBox<String> cmbUserFilter;
    private BorderPane usersRoot;

    public ContentManager(BookController bookController, PrestitoController prestitoController, UtenteController utenteController) {
        this.bookController = bookController;
        this.prestitoController = prestitoController;
        this.utenteController = utenteController;

        this.catalogData = FXCollections.observableArrayList();
        this.loansData = FXCollections.observableArrayList();
        this.usersData = FXCollections.observableArrayList();
    }

    public void inizializzaContenuto(BorderPane root) {
        this.rootContainer = root;

        // Loop di login: chiedi credenziali + tema finché non sono valide o l'utente chiude
        while (true) {
            StartupDialog dlg = new StartupDialog();
            Optional<StartupResult> res = dlg.showAndWait();

            if (res.isEmpty()) {
                Platform.exit(); // utente ha chiuso il dialog
                return;
            }
            StartupResult r = res.get();
            if (r == null || !r.isValid()) {
                showError("Inserisci username e password.");
                continue;
            }

            boolean ok = DatabaseConfig.testCredentials(r.getUsername(), r.getPassword());
            if (ok) {
                DatabaseConfig.apply(r); // salva user/pass
                this.currentTheme = r.getTheme();
                applyTheme(); // applica CSS e style class al root
                break; // esci dal loop e costruisci la UI
            } else {
                showError("Credenziali non valide. Riprova.");
            }
        }

        // Costruzione UI principale
        tabPane = new TabPane();

        // Home migliorata, non chiudibile
        homeTab = new Tab("Home", buildHomeView());
        homeTab.setClosable(false);

        // Avvio con sola Home aperta
        tabPane.getTabs().add(homeTab);

        // Sidebar sinistra con "Esci" sotto "Utenti"
        VBox leftBar = buildLeftSidebar();

        rootContainer.setLeft(leftBar);
        rootContainer.setCenter(tabPane);

        if (rootContainer.getScene() == null) {
            Platform.runLater(this::applyTheme);
        }
    }

    private VBox buildLeftSidebar() {
        Button btnHome = new Button("Home");
        btnHome.setMaxWidth(Double.MAX_VALUE);
        btnHome.setOnAction(e -> mostraHome());

        Button btnCatalog = new Button("Catalogo");
        btnCatalog.setMaxWidth(Double.MAX_VALUE);
        btnCatalog.setOnAction(e -> mostraCatalogoLibri());

        Button btnLoans = new Button("Prestiti");
        btnLoans.setMaxWidth(Double.MAX_VALUE);
        btnLoans.setOnAction(e -> mostraPrestiti());

        Button btnUsers = new Button("Utenti");
        btnUsers.setMaxWidth(Double.MAX_VALUE);
        btnUsers.setOnAction(e -> mostraUtenti());

        Button btnExit = new Button("Esci");
        btnExit.setMaxWidth(Double.MAX_VALUE);
        btnExit.setOnAction(e -> Platform.exit());

        VBox left = new VBox(8, btnHome, btnCatalog, btnLoans, btnUsers, btnExit);
        left.setPadding(new Insets(10));
        left.setFillWidth(true);
        left.getStyleClass().add("sidebar"); // per styling CSS se serve
        return left;
    }

    private VBox buildHomeView() {
        Label title = new Label("Benvenuto nella Biblioteca");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label info = new Label(
                "Cosa puoi fare:\n" +
                        "• Gestire il catalogo libri (aggiungere, modificare, rimuovere).\n" +
                        "• Registrare prestiti e restituzioni.\n" +
                        "• Gestire gli utenti (creazione, modifica, eliminazione).\n\n" +
                        "Limitazioni e note:\n" +
                        "• Alcune operazioni richiedono connessione attiva al database.\n" +
                        "• Non è possibile rimuovere un libro con prestiti attivi.\n" +
                        "• Verifica sempre le date di attivazione/scadenza per gli utenti."
        );
        info.setWrapText(true);

        Button btnGoCatalog = new Button("Apri Catalogo");
        btnGoCatalog.setOnAction(e -> mostraCatalogoLibri());

        Button btnGoLoans = new Button("Apri Prestiti");
        btnGoLoans.setOnAction(e -> mostraPrestiti());

        Button btnGoUsers = new Button("Apri Utenti");
        btnGoUsers.setOnAction(e -> mostraUtenti());

        Button btnExit = new Button("Esci");
        btnExit.setOnAction(e -> Platform.exit()); // chiusura applicazione

        HBox actions = new HBox(10, btnGoCatalog, btnGoLoans, btnGoUsers, btnExit);
        actions.setAlignment(Pos.CENTER);

        VBox box = new VBox(15, title, info, actions);
        box.setPadding(new Insets(20));
        box.setAlignment(Pos.CENTER);
        return box;
    }

    public void mostraHome() {
        tabPane.getSelectionModel().select(homeTab);
    }

    public void mostraCatalogoLibri() {
        ensureCatalogTab();
        tabPane.getSelectionModel().select(catalogTab);
        aggiornaCatalogoLibri();
    }

    public void mostraPrestiti() {
        ensureLoansTab();
        tabPane.getSelectionModel().select(loansTab);
        aggiornaPrestiti();
    }

    public void mostraUtenti() {
        ensureUsersTab();
        tabPane.getSelectionModel().select(usersTab);
        aggiornaUtenti();
    }

    public void cambiaTema(Theme theme) {
        this.currentTheme = theme != null ? theme : Theme.COLORI;
        applyTheme();
    }

    private void applyTheme() {
        if (rootContainer == null) return;

        rootContainer.getStyleClass().removeAll(THEME_COLORI_CLASS, THEME_BW_CLASS);
        String themeClass = (currentTheme == Theme.BIANCO_NERO) ? THEME_BW_CLASS : THEME_COLORI_CLASS;
        if (!rootContainer.getStyleClass().contains(themeClass)) {
            rootContainer.getStyleClass().add(themeClass);
        }

        rootContainer.getStylesheets().removeIf(s -> s.endsWith("theme-color.css") || s.endsWith("theme-bw.css"));
        Scene scene = rootContainer.getScene();
        if (scene != null) {
            scene.getStylesheets().removeIf(s -> s.endsWith("theme-color.css") || s.endsWith("theme-bw.css"));
        }

        String cssPath = (currentTheme == Theme.BIANCO_NERO) ? THEME_BW_CSS : THEME_COLORI_CSS;
        URL url = resolveCss(cssPath);

        if (url != null) {
            String external = url.toExternalForm();
            if (scene != null) {
                if (!scene.getStylesheets().contains(external)) {
                    scene.getStylesheets().add(external);
                }
            } else {
                if (!rootContainer.getStylesheets().contains(external)) {
                    rootContainer.getStylesheets().add(external);
                }
            }
            rootContainer.setStyle("");
        } else {
            System.err.println("WARN: tema CSS non trovato sul classpath: " + cssPath + " — applico fallback inline.");
            if (currentTheme == Theme.BIANCO_NERO) {
                rootContainer.setStyle("-fx-accent: #000000; -fx-focus-color: #000000; -fx-base: #f2f2f2;");
            } else {
                rootContainer.setStyle("-fx-accent: #3f51b5; -fx-focus-color: #3f51b5; -fx-base: #f6f7fb;");
            }
        }
    }

    private URL resolveCss(String path) {
        URL u = getClass().getResource(path);
        if (u == null) {
            String noSlash = path.startsWith("/") ? path.substring(1) : path;
            u = getClass().getResource("/" + noSlash);
            if (u == null) {
                u = Thread.currentThread().getContextClassLoader().getResource(noSlash);
                if (u == null) {
                    u = ContentManager.class.getResource(path);
                }
            }
        }
        return u;
    }

    private void ensureCatalogTab() {
        if (catalogTab == null) {
            catalogTab = new Tab("Catalogo");
            catalogTab.setClosable(true); // chiudibile
        }
        if (!tabPane.getTabs().contains(catalogTab)) tabPane.getTabs().add(catalogTab);
        if (catalogRoot == null) {
            buildCatalogView();
            catalogTab.setContent(catalogRoot);
        }
    }

    private void ensureLoansTab() {
        if (loansTab == null) {
            loansTab = new Tab("Prestiti");
            loansTab.setClosable(true); // chiudibile
        }
        if (!tabPane.getTabs().contains(loansTab)) tabPane.getTabs().add(loansTab);
        if (loansRoot == null) {
            buildLoansView();
            loansTab.setContent(loansRoot);
        }
    }

    private void ensureUsersTab() {
        if (usersTab == null) {
            usersTab = new Tab("Utenti");
            usersTab.setClosable(true); // chiudibile
        }
        if (!tabPane.getTabs().contains(usersTab)) tabPane.getTabs().add(usersTab);
        if (usersRoot == null) {
            buildUsersView();
            usersTab.setContent(usersRoot);
        }
    }

    private void buildCatalogView() {
        catalogRoot = new BorderPane();
        catalogRoot.setPadding(new Insets(10));

        Button btnAdd = new Button("Aggiungi Libro");
        Button btnEdit = new Button("Modifica Libro");
        Button btnRemove = new Button("Rimuovi Libro");
        txtSearchCatalog = new TextField();
        txtSearchCatalog.setPromptText("Cerca nel catalogo...");
        HBox toolbar = new HBox(10, btnAdd, btnEdit, btnRemove, new Label("Ricerca:"), txtSearchCatalog);
        toolbar.setPadding(new Insets(0, 0, 10, 0));
        toolbar.getStyleClass().add("toolbar");

        catalogTable = new TableView<>();
        catalogTable.setPlaceholder(new Label("Nessun libro da mostrare"));

        if (catalogData == null) {
            catalogData = FXCollections.observableArrayList();
        }
        catalogFiltered = new FilteredList<>(catalogData, b -> true);
        catalogSorted = new SortedList<>(catalogFiltered);
        catalogSorted.comparatorProperty().bind(catalogTable.comparatorProperty());

        TableColumn<Book, String> isbnCol = new TableColumn<>("ISBN");
        isbnCol.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        TableColumn<Book, String> titoloCol = new TableColumn<>("Titolo");
        titoloCol.setCellValueFactory(new PropertyValueFactory<>("titolo"));
        TableColumn<Book, String> autoreCol = new TableColumn<>("Autore");
        autoreCol.setCellValueFactory(new PropertyValueFactory<>("autore"));
        TableColumn<Book, LocalDate> dataCol = new TableColumn<>("Pubblicazione");
        dataCol.setCellValueFactory(new PropertyValueFactory<>("dataPubblicazione"));
        TableColumn<Book, String> editoreCol = new TableColumn<>("Casa Editrice");
        editoreCol.setCellValueFactory(new PropertyValueFactory<>("casaEditrice"));
        TableColumn<Book, Integer> copieCol = new TableColumn<>("Copie");
        copieCol.setCellValueFactory(new PropertyValueFactory<>("copie"));

        catalogTable.getColumns().addAll(isbnCol, titoloCol, autoreCol, dataCol, editoreCol, copieCol);
        catalogTable.setItems(catalogSorted);

        txtSearchCatalog.textProperty().addListener((obs, o, val) -> {
            String q = val == null ? "" : val.trim().toLowerCase();
            catalogFiltered.setPredicate(makeBookPredicate(q));
        });

        btnAdd.setOnAction(e -> {
            AddBookDialog dialog = new AddBookDialog();
            dialog.showAndWait().ifPresent(bean -> {
                boolean ok = bookController.aggiungiLibro(bean);
                if (ok) { aggiornaCatalogoLibri(); showInfo("Libro aggiunto."); }
                else showError("Impossibile aggiungere il libro.");
            });
        });

        btnEdit.setOnAction(e -> {
            Book selected = catalogTable.getSelectionModel().getSelectedItem();
            if (selected == null) { showError("Seleziona un libro da modificare."); return; }
            EditBookDialog dialog = new EditBookDialog(selected);
            dialog.showAndWait().ifPresent(bean -> {
                boolean ok = bookController.aggiornaLibro(bean);
                if (ok) { aggiornaCatalogoLibri(); showInfo("Libro aggiornato."); }
                else showError("Impossibile aggiornare il libro.");
            });
        });

        btnRemove.setOnAction(e -> {
            Book selected = catalogTable.getSelectionModel().getSelectedItem();
            if (selected == null) { showError("Seleziona un libro da rimuovere."); return; }
            boolean ok = bookController.rimuoviLibro(selected.getId());
            if (ok) { aggiornaCatalogoLibri(); showInfo("Libro rimosso dal database."); }
            else showError("Impossibile rimuovere il libro. Verifica che non abbia prestiti attivi.");
        });

        catalogRoot.setTop(toolbar);
        catalogRoot.setCenter(catalogTable);
    }

    private void buildLoansView() {
        loansRoot = new BorderPane();
        loansRoot.setPadding(new Insets(10));

        Button btnAddLoan = new Button("Registra Prestito");
        Button btnReturn = new Button("Registra Restituzione");
        Button btnRefresh = new Button("Aggiorna");
        cmbLoanFilter = new ComboBox<>();
        cmbLoanFilter.getItems().addAll("Tutti", "Attivi", "Non attivi");
        cmbLoanFilter.getSelectionModel().select("Tutti");
        txtSearchLoans = new TextField();
        txtSearchLoans.setPromptText("Cerca nei prestiti...");

        HBox toolbar = new HBox(10, btnAddLoan, btnReturn, btnRefresh,
                new Label("Filtro:"), cmbLoanFilter,
                new Label("Ricerca:"), txtSearchLoans);
        toolbar.setPadding(new Insets(0, 0, 10, 0));
        toolbar.getStyleClass().add("toolbar");

        loansTable = new TableView<>();
        loansTable.setPlaceholder(new Label("Nessun prestito da mostrare"));

        if (loansData == null) {
            loansData = FXCollections.observableArrayList();
        }
        loansFiltered = new FilteredList<>(loansData, p -> true);
        loansSorted = new SortedList<>(loansFiltered);
        loansSorted.comparatorProperty().bind(loansTable.comparatorProperty());
        loansTable.setItems(loansSorted);

        TableColumn<Prestito, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Prestito, String> libroCol = new TableColumn<>("Libro");
        libroCol.setCellValueFactory(new PropertyValueFactory<>("libroTitoloSnapshot"));
        TableColumn<Prestito, String> utenteCol = new TableColumn<>("Utente");
        utenteCol.setCellValueFactory(new PropertyValueFactory<>("utente"));
        TableColumn<Prestito, LocalDate> dataPrestitoCol = new TableColumn<>("Data Prestito");
        dataPrestitoCol.setCellValueFactory(new PropertyValueFactory<>("dataPrestito"));
        TableColumn<Prestito, LocalDate> dataRestituzioneCol = new TableColumn<>("Data Restituzione");
        dataRestituzioneCol.setCellValueFactory(new PropertyValueFactory<>("dataRestituzione"));

        loansTable.getColumns().addAll(idCol, libroCol, utenteCol, dataPrestitoCol, dataRestituzioneCol);

        cmbLoanFilter.valueProperty().addListener((obs, o, v) -> applyLoansPredicate());
        txtSearchLoans.textProperty().addListener((obs, o, v) -> applyLoansPredicate());

        btnAddLoan.setOnAction(e -> {
            List<Book> tutti = bookController.trovaTutti();
            Map<Long, Long> attiviPerLibro = prestitoController.trovaPrestitiAttivi().stream()
                    .filter(p -> p.getLibroId() != null)
                    .collect(Collectors.groupingBy(Prestito::getLibroId, Collectors.counting()));

            List<Book> disponibili = tutti.stream()
                    .filter(b -> {
                        long cnt = attiviPerLibro.getOrDefault(b.getId(), 0L);
                        return b.getId() != null && cnt < b.getCopie();
                    })
                    .collect(Collectors.toList());

            if (disponibili.isEmpty()) { showError("Nessun libro disponibile (tutte le copie sono in prestito)."); return; }

            SelectBookDialog selectBook = new SelectBookDialog(disponibili);
            selectBook.showAndWait().ifPresent(selectedBook -> {
                SelectUserDialog selectUser = new SelectUserDialog(utenteController, this::aggiornaUtenti);
                selectUser.showAndWait().ifPresent(selectedUser -> {
                    PrestitoDialog dlg = new PrestitoDialog(selectedBook, selectedUser);
                    dlg.showAndWait().ifPresent(bean -> {
                        PrestitoController.Esito esito = prestitoController.registraPrestito(bean);
                        switch (esito) {
                            case OK -> {
                                aggiornaPrestiti();
                                aggiornaCatalogoLibri();
                                showInfo("Prestito registrato con successo.");
                                tabPane.getSelectionModel().select(loansTab);
                            }
                            case UTENTE_INATTIVO -> showError("Impossibile registrare il prestito: utente non attivo.");
                            case ERRORE_INSERIMENTO -> showError("Impossibile registrare il prestito. Verifica che il database sia allineato e i riferimenti validi.");
                        }
                    });
                });
            });
        });

        btnReturn.setOnAction(e -> {
            Prestito sel = loansTable.getSelectionModel().getSelectedItem();
            if (sel == null) { showError("Seleziona un prestito da chiudere."); return; }
            boolean ok = prestitoController.registraRestituzione(sel.getId(), LocalDate.now());
            if (ok) { aggiornaPrestiti(); aggiornaCatalogoLibri(); showInfo("Restituzione registrata."); }
            else showError("Impossibile registrare la restituzione.");
        });

        btnRefresh.setOnAction(e -> aggiornaPrestiti());

        loansRoot.setTop(toolbar);
        loansRoot.setCenter(loansTable);
    }

    private void buildUsersView() {
        usersRoot = new BorderPane();
        usersRoot.setPadding(new Insets(10));

        Button btnAdd = new Button("Nuovo");
        Button btnEdit = new Button("Modifica");
        Button btnDelete = new Button("Elimina");
        txtSearchUsers = new TextField();
        txtSearchUsers.setPromptText("Cerca utenti...");
        cmbUserFilter = new ComboBox<>();
        cmbUserFilter.getItems().addAll("Tutti", "Attivi", "Inattivi");
        cmbUserFilter.getSelectionModel().select("Tutti");

        HBox toolbar = new HBox(10, btnAdd, btnEdit, btnDelete, new Label("Filtro:"), cmbUserFilter, new Label("Ricerca:"), txtSearchUsers);
        toolbar.setPadding(new Insets(0, 0, 10, 0));
        toolbar.getStyleClass().add("toolbar");
        usersRoot.setTop(toolbar);

        usersTable = new TableView<>();
        usersTable.setPlaceholder(new Label("Nessun utente"));

        if (usersData == null) {
            usersData = FXCollections.observableArrayList();
        }

        TableColumn<Utente, Integer> tesseraCol = new TableColumn<>("Tessera");
        tesseraCol.setCellValueFactory(new PropertyValueFactory<>("tessera"));
        TableColumn<Utente, String> nomeCol = new TableColumn<>("Nome");
        nomeCol.setCellValueFactory(new PropertyValueFactory<>("nome"));
        TableColumn<Utente, String> cognomeCol = new TableColumn<>("Cognome");
        cognomeCol.setCellValueFactory(new PropertyValueFactory<>("cognome"));
        TableColumn<Utente, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        TableColumn<Utente, String> telCol = new TableColumn<>("Telefono");
        telCol.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        TableColumn<Utente, LocalDate> attCol = new TableColumn<>("Attivazione");
        attCol.setCellValueFactory(new PropertyValueFactory<>("dataAttivazione"));
        TableColumn<Utente, LocalDate> scadCol = new TableColumn<>("Scadenza");
        scadCol.setCellValueFactory(new PropertyValueFactory<>("dataScadenza"));
        TableColumn<Utente, String> statoCol = new TableColumn<>("Stato");
        statoCol.setCellValueFactory(new PropertyValueFactory<>("stato"));

        usersTable.getColumns().addAll(tesseraCol, nomeCol, cognomeCol, emailCol, telCol, attCol, scadCol, statoCol);

        usersFiltered = new FilteredList<>(usersData, u -> true);
        usersSorted = new SortedList<>(usersFiltered);
        usersSorted.comparatorProperty().bind(usersTable.comparatorProperty());
        usersTable.setItems(usersSorted);

        txtSearchUsers.textProperty().addListener((obs, o, v) -> applyUsersPredicate());
        cmbUserFilter.valueProperty().addListener((obs, o, v) -> applyUsersPredicate());

        btnAdd.setOnAction(e -> {
            AddEditUserDialog dlg = new AddEditUserDialog(utenteController, null);
            dlg.showAndWait().ifPresent(bean -> {
                if (utenteController.aggiungi(bean)) {
                    aggiornaUtenti();
                    showInfo("Utente aggiunto.");
                } else {
                    showError("Impossibile aggiungere l'utente (dati non validi o tessera duplicata?).");
                }
            });
        });

        btnEdit.setOnAction(e -> {
            Utente sel = usersTable.getSelectionModel().getSelectedItem();
            if (sel == null) { showError("Seleziona un utente da modificare."); return; }
            AddEditUserDialog dlg = new AddEditUserDialog(utenteController, sel);
            dlg.showAndWait().ifPresent(bean -> {
                bean.setId(sel.getId());
                if (utenteController.aggiorna(bean)) {
                    aggiornaUtenti();
                    showInfo("Utente aggiornato.");
                } else {
                    showError("Impossibile aggiornare l'utente (verifica le date).");
                }
            });
        });

        btnDelete.setOnAction(e -> {
            Utente sel = usersTable.getSelectionModel().getSelectedItem();
            if (sel == null) { showError("Seleziona un utente da eliminare."); return; }
            if (utenteController.elimina(sel.getId())) {
                aggiornaUtenti();
                showInfo("Utente eliminato.");
            } else {
                showError("Impossibile eliminare l'utente.");
            }
        });

        usersRoot.setCenter(usersTable);
    }

    private Predicate<Book> makeBookPredicate(String q) {
        if (q == null || q.isBlank()) return b -> true;
        return b -> {
            String s = q.toLowerCase();
            return (b.getIsbn() != null && b.getIsbn().toLowerCase().contains(s)) ||
                    (b.getTitolo() != null && b.getTitolo().toLowerCase().contains(s)) ||
                    (b.getAutore() != null && b.getAutore().toLowerCase().contains(s)) ||
                    (b.getCasaEditrice() != null && b.getCasaEditrice().toLowerCase().contains(s)) ||
                    (b.getDataPubblicazione() != null && b.getDataPubblicazione().toString().contains(s)) ||
                    String.valueOf(b.getCopie()).contains(s);
        };
    }

    private java.util.function.Predicate<Utente> makeUserPredicate(String q, String statoFilter) {
        return u -> {
            boolean attivo = u.isAttivo();
            boolean statoOk = "Tutti".equals(statoFilter) ||
                    ("Attivi".equals(statoFilter) && attivo) ||
                    ("Inattivi".equals(statoFilter) && !attivo);
            if (!statoOk) return false;

            if (q == null || q.isBlank()) return true;
            String s = q.toLowerCase();
            String tessera = u.getTessera() != null ? String.valueOf(u.getTessera()) : "";
            String nome = u.getNome() != null ? u.getNome().toLowerCase() : "";
            String cognome = u.getCognome() != null ? u.getCognome().toLowerCase() : "";
            String email = u.getEmail() != null ? u.getEmail().toLowerCase() : "";
            String tel = u.getTelefono() != null ? u.getTelefono().toLowerCase() : "";
            String att = u.getDataAttivazione() != null ? u.getDataAttivazione().toString() : "";
            String scad = u.getDataScadenza() != null ? u.getDataScadenza().toString() : "";
            String stato = u.getStato().toLowerCase();

            return tessera.contains(s) || nome.contains(s) || cognome.contains(s) ||
                    email.contains(s) || tel.contains(s) || att.contains(s) || scad.contains(s) ||
                    stato.contains(s);
        };
    }

    private void applyLoansPredicate() {
        if (loansFiltered == null) return;
        String filter = cmbLoanFilter != null ? cmbLoanFilter.getSelectionModel().getSelectedItem() : "Tutti";
        String q = (txtSearchLoans != null && txtSearchLoans.getText() != null) ? txtSearchLoans.getText().trim().toLowerCase() : "";

        loansFiltered.setPredicate(p -> {
            boolean isActive = p.getDataRestituzione() == null;
            boolean passFilter =
                    "Tutti".equals(filter) ||
                            ("Attivi".equals(filter) && isActive) ||
                            ("Non attivi".equals(filter) && !isActive);
            if (!passFilter) return false;

            if (q.isBlank()) return true;

            String idStr = p.getId() != null ? String.valueOf(p.getId()) : "";
            String libro = p.getLibroTitoloSnapshot() != null ? p.getLibroTitoloSnapshot().toLowerCase() : "";
            String utente = p.getUtente() != null ? p.getUtente().toLowerCase() : "";
            String dp = p.getDataPrestito() != null ? p.getDataPrestito().toString() : "";
            String dr = p.getDataRestituzione() != null ? p.getDataRestituzione().toString() : "";

            return idStr.contains(q) || libro.contains(q) || utente.contains(q) || dp.contains(q) || dr.contains(q);
        });
    }

    private void applyUsersPredicate() {
        if (usersFiltered == null) return;
        String stato = (cmbUserFilter != null && cmbUserFilter.getSelectionModel().getSelectedItem() != null)
                ? cmbUserFilter.getSelectionModel().getSelectedItem() : "Tutti";
        String q = (txtSearchUsers != null && txtSearchUsers.getText() != null)
                ? txtSearchUsers.getText().trim().toLowerCase() : "";
        usersFiltered.setPredicate(makeUserPredicate(q, stato));
    }

    private void aggiornaCatalogoLibri() {
        try {
            List<Book> libri = bookController.trovaTutti();
            if (catalogData == null) {
                catalogData = FXCollections.observableArrayList();
            }
            catalogData.setAll(libri);
        } catch (Exception e) {
            showError("Errore nell'aggiornamento del catalogo: " + e.getMessage());
        }
    }

    private void aggiornaPrestiti() {
        try {
            List<Prestito> prestiti = prestitoController.trovaTutti();
            if (loansData == null) {
                loansData = FXCollections.observableArrayList();
            }
            loansData.setAll(prestiti);
            applyLoansPredicate();
        } catch (Exception e) {
            showError("Errore nell'aggiornamento dei prestiti: " + e.getMessage());
        }
    }

    private void aggiornaUtenti() {
        try {
            List<Utente> utenti = utenteController.trovaTutti();
            if (usersData == null) {
                usersData = FXCollections.observableArrayList();
            }
            usersData.setAll(utenti);
            applyUsersPredicate();
        } catch (Exception e) {
            showError("Errore nell'aggiornamento degli utenti: " + e.getMessage());
        }
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.setTitle("Informazione");
        a.showAndWait();
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.setTitle("Errore");
        a.showAndWait();
    }
}