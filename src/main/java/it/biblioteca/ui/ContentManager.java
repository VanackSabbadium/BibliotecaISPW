package it.biblioteca.ui;

import it.biblioteca.bean.BookBean;
import it.biblioteca.bean.PrestitoBean;
import it.biblioteca.controller.BookController;
import it.biblioteca.controller.PrenotazioneController;
import it.biblioteca.controller.PrestitoController;
import it.biblioteca.controller.UtenteController;
import it.biblioteca.entity.Book;
import it.biblioteca.entity.Prestito;
import it.biblioteca.entity.Utente;
import it.biblioteca.security.AuthService;
import it.biblioteca.security.SessionContext;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Pair;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * ContentManager: gestisce la costruzione dinamica dei contenuti (tabs + sidebar)
 * in funzione del ruolo corrente (SessionContext).
 *
 * Nota: il costruttore ora richiede i quattro controller usati nell'app.
 */
public class ContentManager {

    public enum Theme { COLORI, BIANCO_NERO }

    private static final String THEME_COLORI_CSS = "/css/theme-color.css";
    private static final String THEME_BW_CSS     = "/css/theme-bw.css";
    private static final String THEME_COLORI_CLASS = "theme-color";
    private static final String THEME_BW_CLASS     = "theme-bw";

    private final BookController bookController;
    private final PrestitoController prestitoController;
    private final UtenteController utenteController;
    private final PrenotazioneController prenotazioneController;

    // UI root references
    private BorderPane rootContainer;
    private TabPane tabPane;
    private Tab homeTab;
    private Tab catalogTab;
    private Tab loansTab;
    private Tab usersTab;
    private Tab profileTab;
    private Tab myLoansTab; // tab specifica per utente (i miei prestiti)

    private Theme currentTheme = Theme.COLORI;

    // Catalogo: fields (in modo da poterli abilitare/disabilitare in updateUIForRole)
    private TableView<Book> catalogTable;
    private ObservableList<Book> catalogData;
    private FilteredList<Book> catalogFiltered;
    private SortedList<Book> catalogSorted;
    private TextField txtSearchCatalog;
    private BorderPane catalogRoot;

    // Catalog buttons (campi per poterli abilitare/disabilitare)
    private Button btnAddBook;
    private Button btnEditBook;
    private Button btnRemoveBook;

    // Prestiti (bibliotecario)
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

    // Miei prestiti (utente)
    private TableView<Prestito> myLoansTable;
    private ObservableList<Prestito> myLoansData;
    private FilteredList<Prestito> myLoansFiltered;
    private SortedList<Prestito> myLoansSorted;
    private TextField txtSearchMyLoans;
    private ComboBox<String> cmbMyLoansFilter;
    private BorderPane myLoansRoot;

    public ContentManager(BookController bookController,
                          PrestitoController prestitoController,
                          UtenteController utenteController,
                          PrenotazioneController prenotazioneController) {
        this.bookController = bookController;
        this.prestitoController = prestitoController;
        this.utenteController = utenteController;
        this.prenotazioneController = prenotazioneController;

        this.catalogData = FXCollections.observableArrayList();
        this.loansData = FXCollections.observableArrayList();
        this.usersData = FXCollections.observableArrayList();
        this.myLoansData = FXCollections.observableArrayList();
    }

    public ContentManager(BookController bookController,
                          PrestitoController prestitoController,
                          UtenteController utenteController) {
        this(bookController, prestitoController, utenteController, null);
    }

    /**
     * Inizializza il contenuto (esegue login DB tramite StartupDialog,
     * poi login applicativo tramite AuthService e infine costruisce UI).
     */
    public void inizializzaContenuto(BorderPane root) {
        this.rootContainer = root;

        // 1) Login / configurazione DB (riuso StartupDialog per ottenere username/password e tema)
        while (true) {
            StartupDialog dlg = new StartupDialog();
            Optional<StartupResult> res = dlg.showAndWait();

            if (res.isEmpty()) {
                Platform.exit();
                return;
            }
            StartupResult r = res.get();
            if (r == null || !r.isValid()) {
                showError("Inserisci username e password per il DB.");
                continue;
            }

            // Prova a configurare le credenziali JDBC
            boolean ok = it.biblioteca.db.DatabaseConfig.testCredentials(r.getUsername(), r.getPassword());
            if (ok) {
                it.biblioteca.db.DatabaseConfig.apply(r);
                this.currentTheme = r.getTheme();
                applyTheme();
                break;
            } else {
                showError("Credenziali DB non valide. Riprova.");
            }
        }

        // 2) Login applicativo (AuthService) - richiedi username/password dell'app
        boolean appLogged = performAppLogin();
        if (!appLogged) {
            // se l'utente ha annullato, esci
            Platform.exit();
            return;
        }

        // 3) Costruzione UI base
        tabPane = new TabPane();
        homeTab = new Tab("Home", buildHomeView());
        homeTab.setClosable(false);
        tabPane.getTabs().add(homeTab);

        // Left sidebar dinamico
        VBox leftBar = buildLeftSidebar();
        root.setLeft(leftBar);

        // center
        root.setCenter(tabPane);

        // inizializza tabs lazy
        // Carica contenuti iniziali
        aggiornaCatalogoLibri();
        aggiornaPrestiti();
        aggiornaUtenti();
        aggiornaMyPrestiti(); // carica anche i miei prestiti (se utente)
    }

    // -------------------------
    // Login applicativo con AuthService
    // -------------------------
    private boolean performAppLogin() {
        // Dialog semplice per credenziali applicative (utente/password)
        Dialog<Pair<String, String>> dlg = new Dialog<>();
        dlg.setTitle("Login Applicativo");
        dlg.setHeaderText("Inserisci username e password applicative");

        ButtonType okType = new ButtonType("Accedi", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(okType, ButtonType.CANCEL);

        GridPane g = new GridPane();
        g.setHgap(8); g.setVgap(8); g.setPadding(new Insets(10));

        TextField txtUser = new TextField();
        txtUser.setPromptText("Username");
        PasswordField txtPass = new PasswordField();
        txtPass.setPromptText("Password");

        g.add(new Label("Username:"), 0, 0);
        g.add(txtUser, 1, 0);
        g.add(new Label("Password:"), 0, 1);
        g.add(txtPass, 1, 1);

        dlg.getDialogPane().setContent(g);

        // Abilita ok solo se riempiti
        Node okBtn = dlg.getDialogPane().lookupButton(okType);
        okBtn.setDisable(true);
        Runnable validate = () -> okBtn.setDisable(txtUser.getText().isBlank() || txtPass.getText().isBlank());
        txtUser.textProperty().addListener((o, a, b) -> validate.run());
        txtPass.textProperty().addListener((o, a, b) -> validate.run());
        validate.run();

        dlg.setResultConverter(bt -> bt == okType ? new Pair<>(txtUser.getText().trim(), txtPass.getText()) : null);

        Optional<Pair<String, String>> res = dlg.showAndWait();
        if (res.isEmpty()) return false;
        Pair<String, String> pair = res.get();

        AuthService.AuthResult ar = AuthService.authenticate(pair.getKey(), pair.getValue());
        if (!ar.ok) {
            showError("Credenziali applicative non valide.");
            return performAppLogin(); // riprova (ricorsione semplice)
        }

        // Imposta SessionContext
        SessionContext.setRole(ar.role);
        SessionContext.setUserId(ar.userId);
        SessionContext.setTessera(ar.tessera);

        // Ri-configura UI in base al ruolo (sidebar e tabs)
        updateUIForRole();
        return true;
    }

    // -------------------------
    // Left sidebar costruita dinamicamente
    // -------------------------
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

        Button btnProfile = new Button("Il mio profilo");
        btnProfile.setMaxWidth(Double.MAX_VALUE);
        btnProfile.setOnAction(e -> mostraProfiloUtente());

        Button btnMyLoans = new Button("I miei prestiti");
        btnMyLoans.setMaxWidth(Double.MAX_VALUE);
        btnMyLoans.setOnAction(e -> mostraMieiPrestiti());

        Button btnExit = new Button("Esci");
        btnExit.setMaxWidth(Double.MAX_VALUE);
        btnExit.setOnAction(e -> Platform.exit());

        VBox left = new VBox(8);
        left.setPadding(new Insets(10));
        left.setFillWidth(true);
        left.getStyleClass().add("sidebar");

        // Scegli cosa mostrare in base al ruolo attuale
        if (SessionContext.isAdmin()) {
            left.getChildren().addAll(btnHome, btnUsers, btnExit);
        } else if (SessionContext.isBibliotecario()) {
            left.getChildren().addAll(btnHome, btnCatalog, btnLoans, btnUsers, btnExit);
        } else if (SessionContext.isUtente()) {
            left.getChildren().addAll(btnHome, btnCatalog, btnMyLoans, btnProfile, btnExit);
        } else {
            left.getChildren().addAll(btnHome, btnExit);
        }

        return left;
    }

    // -------------------------
    // Home view (aggiornabile)
    // -------------------------
    private BorderPane buildHomeView() {
        BorderPane p = new BorderPane();
        p.setPadding(new Insets(20));
        Label title = new Label("Benvenuto nella Biblioteca");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label info = new Label(getHomeDescriptionForRole());
        info.setWrapText(true);

        Button btnGoCatalog = new Button("Apri Catalogo");
        btnGoCatalog.setOnAction(e -> mostraCatalogoLibri());
        Button btnGoLoans = new Button("Apri Prestiti");
        btnGoLoans.setOnAction(e -> mostraPrestiti());
        Button btnGoUsers = new Button("Apri Utenti");
        btnGoUsers.setOnAction(e -> mostraUtenti());
        Button btnExit = new Button("Esci");
        btnExit.setOnAction(e -> Platform.exit());

        HBox actions = new HBox(10, btnGoCatalog, btnGoLoans, btnGoUsers, btnExit);
        actions.setAlignment(Pos.CENTER);

        VBox box = new VBox(15, title, info, actions);
        box.setPadding(new Insets(20));
        box.setAlignment(Pos.CENTER);

        p.setCenter(box);
        return p;
    }

    private String getHomeDescriptionForRole() {
        if (SessionContext.isBibliotecario()) {
            return "Sei connesso come Bibliotecario. Puoi gestire catalogo, prestiti e utenti.";
        } else if (SessionContext.isAdmin()) {
            return "Sei connesso come Admin. Puoi creare/modificare/rimuovere le credenziali degli utenti (create dal Bibliotecario).";
        } else if (SessionContext.isUtente()) {
            return "Sei connesso come Utente. Puoi visualizzare il catalogo, visualizzare il tuo profilo e i tuoi prestiti (attivi e storici), e prenotare libri se le copie sono terminate.";
        } else {
            return "Effettua il login per continuare.";
        }
    }

    private void updateHomeDescription() {
        if (homeTab != null && homeTab.getContent() instanceof BorderPane) {
            homeTab.setContent(buildHomeView());
        }
    }

    // -------------------------
    // Manage Catalog Tab
    // -------------------------
    public void mostraCatalogoLibri() {
        ensureCatalogTab();
        tabPane.getSelectionModel().select(catalogTab);
        aggiornaCatalogoLibri();
    }

    private void ensureCatalogTab() {
        if (catalogTab == null) {
            catalogTab = new Tab("Catalogo");
            catalogTab.setClosable(true);
            buildCatalogView();
            catalogTab.setContent(catalogRoot);
        }
        if (!tabPane.getTabs().contains(catalogTab)) tabPane.getTabs().add(catalogTab);
    }

    private void buildCatalogView() {
        catalogRoot = new BorderPane();
        catalogRoot.setPadding(new Insets(10));

        btnAddBook = new Button("Aggiungi Libro");
        btnEditBook = new Button("Modifica Libro");
        btnRemoveBook = new Button("Rimuovi Libro");
        txtSearchCatalog = new TextField();
        txtSearchCatalog.setPromptText("Cerca nel catalogo...");
        HBox toolbar = new HBox(10, btnAddBook, btnEditBook, btnRemoveBook, new Label("Ricerca:"), txtSearchCatalog);
        toolbar.setPadding(new Insets(0, 0, 10, 0));
        toolbar.getStyleClass().add("toolbar");

        catalogTable = new TableView<>();
        catalogTable.setPlaceholder(new Label("Nessun libro da mostrare"));

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

// Colonna copie totali (solo per bibliotecario)
        TableColumn<Book, Integer> copieTotCol = new TableColumn<>("Copie totali");
        copieTotCol.setCellValueFactory(new PropertyValueFactory<>("copie"));

// Colonna copie disponibili (sempre visibile)
        TableColumn<Book, Integer> copieDispCol = new TableColumn<>("Copie disponibili");
        copieDispCol.setCellValueFactory(cell -> {
            Book b = cell.getValue();
            int copies = (b != null) ? b.getCopie() : 0;
            long active = 0L;
            try {
                List<Prestito> attivi = prestitoController.trovaPrestitiAttivi();
                if (b != null && b.getId() != null) {
                    active = attivi.stream()
                            .filter(p -> p.getLibroId() != null && b.getId().equals(p.getLibroId()))
                            .count();
                }
            } catch (Exception ex) {
                System.err.println("Warning: impossibile calcolare prestiti attivi: " + ex.getMessage());
            }
            int avail = (int) Math.max(0, copies - active);
            return new ReadOnlyObjectWrapper<>(avail);
        });

// Aggiunta colonne in base al ruolo
        if (SessionContext.isBibliotecario()) {
            catalogTable.getColumns().addAll(isbnCol, titoloCol, autoreCol, dataCol, editoreCol, copieTotCol, copieDispCol);
        } else {
            catalogTable.getColumns().addAll(isbnCol, titoloCol, autoreCol, dataCol, editoreCol, copieDispCol);
        }

        catalogTable.setItems(catalogSorted);

        txtSearchCatalog.textProperty().addListener((obs, o, val) -> {
            String q = val == null ? "" : val.trim().toLowerCase();
            catalogFiltered.setPredicate(makeBookPredicate(q));
        });

        btnAddBook.setOnAction(e -> {
            if (!SessionContext.isBibliotecario()) { showError("Operazione consentita solo al Bibliotecario."); return; }
            AddBookDialog dialog = new AddBookDialog();
            dialog.showAndWait().ifPresent(bean -> {
                boolean ok = bookController.aggiungiLibro(bean);
                if (ok) { aggiornaCatalogoLibri(); showInfo("Libro aggiunto."); }
                else showError("Impossibile aggiungere il libro.");
            });
        });

        btnEditBook.setOnAction(e -> {
            if (!SessionContext.isBibliotecario()) { showError("Operazione consentita solo al Bibliotecario."); return; }
            Book selected = catalogTable.getSelectionModel().getSelectedItem();
            if (selected == null) { showError("Seleziona un libro da modificare."); return; }
            EditBookDialog dialog = new EditBookDialog(selected);
            dialog.showAndWait().ifPresent(bean -> {
                boolean ok = bookController.aggiornaLibro(bean);
                if (ok) { aggiornaCatalogoLibri(); showInfo("Libro aggiornato."); }
                else showError("Impossibile aggiornare il libro.");
            });
        });

        btnRemoveBook.setOnAction(e -> {
            if (!SessionContext.isBibliotecario()) { showError("Operazione consentita solo al Bibliotecario."); return; }
            Book selected = catalogTable.getSelectionModel().getSelectedItem();
            if (selected == null) { showError("Seleziona un libro da rimuovere."); return; }
            boolean ok = bookController.rimuoviLibro(selected.getId());
            if (ok) { aggiornaCatalogoLibri(); showInfo("Libro rimosso dal database."); }
            else showError("Impossibile rimuovere il libro. Verifica che non abbia prestiti attivi o prenotazioni.");
        });

        catalogRoot.setTop(toolbar);
        catalogRoot.setCenter(catalogTable);

        // Applica restrizioni su pulsanti in base al ruolo attuale
        applyCatalogPermissions();
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

    private void applyCatalogPermissions() {
        boolean isBibliotecario = SessionContext.isBibliotecario();
        if (btnAddBook != null) btnAddBook.setDisable(!isBibliotecario);
        if (btnEditBook != null) btnEditBook.setDisable(!isBibliotecario);
        if (btnRemoveBook != null) btnRemoveBook.setDisable(!isBibliotecario);
    }

    private void aggiornaCatalogoLibri() {
        try {
            List<Book> libri = bookController.trovaTutti();
            if (catalogData == null) catalogData = FXCollections.observableArrayList();
            catalogData.setAll(libri);
        } catch (Exception e) {
            showError("Errore nell'aggiornamento del catalogo: " + e.getMessage());
        }
    }

    // -------------------------
    // Manage Loans Tab (Prestiti) - per Bibliotecario
    // -------------------------
    public void mostraPrestiti() {
        ensureLoansTab();
        tabPane.getSelectionModel().select(loansTab);
        aggiornaPrestiti();
    }

    private void ensureLoansTab() {
        if (loansTab == null) {
            loansTab = new Tab("Prestiti");
            loansTab.setClosable(true);
            buildLoansView();
            loansTab.setContent(loansRoot);
        }
        if (!tabPane.getTabs().contains(loansTab)) tabPane.getTabs().add(loansTab);
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
            // Solo bibliotecario può registrare prestiti
            if (!SessionContext.isBibliotecario()) { showError("Operazione consentita solo al Bibliotecario."); return; }

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
                            case ERRORE_INSERIMENTO -> showError("Impossibile registrare il prestito. Verifica il DB.");
                        }
                    });
                });
            });
        });

        btnReturn.setOnAction(e -> {
            if (!SessionContext.isBibliotecario()) { showError("Operazione consentita solo al Bibliotecario."); return; }
            Prestito sel = loansTable.getSelectionModel().getSelectedItem();
            if (sel == null) { showError("Seleziona un prestito da chiudere."); return; }
            boolean ok = prestitoController.registraRestituzione(sel.getId(), LocalDate.now());
            if (ok) { aggiornaPrestiti(); aggiornaCatalogoLibri(); showInfo("Restituzione registrata."); }
            else showError("Impossibile registrare la restituzione.");
        });

        btnRefresh.setOnAction(e -> aggiornaPrestiti());

        loansRoot.setTop(toolbar);
        loansRoot.setCenter(loansTable);

        // Se non è bibliotecario, disabilitiamo il tab alla creazione
        if (!SessionContext.isBibliotecario()) {
            loansTab.setDisable(true);
        }
    }

    private void applyLoansPredicate() {
        if (loansFiltered == null) return;
        String filter = cmbLoanFilter != null ? cmbLoanFilter.getSelectionModel().getSelectedItem() : "Tutti";
        String q = (txtSearchLoans != null && txtSearchLoans.getText() != null) ? txtSearchLoans.getText().trim().toLowerCase() : "";

        loansFiltered.setPredicate(p -> {
            boolean isActive = p.getDataRestituzione() == null;
            boolean passFilter = "Tutti".equals(filter) ||
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

    private void aggiornaPrestiti() {
        try {
            List<Prestito> prestiti = prestitoController.trovaTutti();
            if (loansData == null) loansData = FXCollections.observableArrayList();
            loansData.setAll(prestiti);
            applyLoansPredicate();
        } catch (Exception e) {
            showError("Errore nell'aggiornamento dei prestiti: " + e.getMessage());
        }
    }

    // -------------------------
    // My Loans (I miei prestiti) - per Utente (solo lettura)
    // -------------------------
    public void mostraMieiPrestiti() {
        ensureMyLoansTab();
        tabPane.getSelectionModel().select(myLoansTab);
        aggiornaMyPrestiti();
    }

    private void ensureMyLoansTab() {
        if (myLoansTab == null) {
            myLoansTab = new Tab("I miei prestiti");
            myLoansTab.setClosable(true);
            buildMyLoansView();
            myLoansTab.setContent(myLoansRoot);
        }
        if (!tabPane.getTabs().contains(myLoansTab)) tabPane.getTabs().add(myLoansTab);
    }

    private void buildMyLoansView() {
        myLoansRoot = new BorderPane();
        myLoansRoot.setPadding(new Insets(10));

        Button btnRefresh = new Button("Aggiorna");
        cmbMyLoansFilter = new ComboBox<>();
        cmbMyLoansFilter.getItems().addAll("Tutti", "Attivi", "Storico");
        cmbMyLoansFilter.getSelectionModel().select("Tutti");
        txtSearchMyLoans = new TextField();
        txtSearchMyLoans.setPromptText("Cerca nei miei prestiti...");

        HBox toolbar = new HBox(10, btnRefresh, new Label("Filtro:"), cmbMyLoansFilter, new Label("Ricerca:"), txtSearchMyLoans);
        toolbar.setPadding(new Insets(0, 0, 10, 0));
        toolbar.getStyleClass().add("toolbar");

        myLoansTable = new TableView<>();
        myLoansTable.setPlaceholder(new Label("Nessun prestito per il tuo account"));

        myLoansFiltered = new FilteredList<>(myLoansData, p -> true);
        myLoansSorted = new SortedList<>(myLoansFiltered);
        myLoansSorted.comparatorProperty().bind(myLoansTable.comparatorProperty());
        myLoansTable.setItems(myLoansSorted);

        TableColumn<Prestito, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Prestito, String> libroCol = new TableColumn<>("Libro");
        libroCol.setCellValueFactory(new PropertyValueFactory<>("libroTitoloSnapshot"));
        TableColumn<Prestito, LocalDate> dataPrestitoCol = new TableColumn<>("Data Prestito");
        dataPrestitoCol.setCellValueFactory(new PropertyValueFactory<>("dataPrestito"));
        TableColumn<Prestito, LocalDate> dataRestituzioneCol = new TableColumn<>("Data Restituzione");
        dataRestituzioneCol.setCellValueFactory(new PropertyValueFactory<>("dataRestituzione"));

        myLoansTable.getColumns().addAll(idCol, libroCol, dataPrestitoCol, dataRestituzioneCol);

        cmbMyLoansFilter.valueProperty().addListener((obs, o, v) -> applyMyLoansPredicate());
        txtSearchMyLoans.textProperty().addListener((obs, o, v) -> applyMyLoansPredicate());

        btnRefresh.setOnAction(e -> aggiornaMyPrestiti());

        myLoansRoot.setTop(toolbar);
        myLoansRoot.setCenter(myLoansTable);
    }

    private void applyMyLoansPredicate() {
        if (myLoansFiltered == null) return;
        String filter = cmbMyLoansFilter != null ? cmbMyLoansFilter.getSelectionModel().getSelectedItem() : "Tutti";
        String q = (txtSearchMyLoans != null && txtSearchMyLoans.getText() != null) ? txtSearchMyLoans.getText().trim().toLowerCase() : "";

        myLoansFiltered.setPredicate(p -> {
            boolean isActive = p.getDataRestituzione() == null;
            boolean passFilter = "Tutti".equals(filter) ||
                    ("Attivi".equals(filter) && isActive) ||
                    ("Storico".equals(filter) && !isActive);
            if (!passFilter) return false;

            if (q.isBlank()) return true;

            String idStr = p.getId() != null ? String.valueOf(p.getId()) : "";
            String libro = p.getLibroTitoloSnapshot() != null ? p.getLibroTitoloSnapshot().toLowerCase() : "";
            String dp = p.getDataPrestito() != null ? p.getDataPrestito().toString() : "";
            String dr = p.getDataRestituzione() != null ? p.getDataRestituzione().toString() : "";

            return idStr.contains(q) || libro.contains(q) || dp.contains(q) || dr.contains(q);
        });
    }

    private void aggiornaMyPrestiti() {
        try {
            Long sessionUtenteId = SessionContext.getUserId(); // ASSUNZIONE: è l'id della riga in 'utenti' (prestiti.utente_id)
            List<Prestito> all = prestitoController.trovaTutti();
            List<Prestito> mine = all.stream()
                    .filter(p -> p.getUtenteId() != null && sessionUtenteId != null && sessionUtenteId.equals(p.getUtenteId()))
                    .collect(Collectors.toList());
            if (myLoansData == null) myLoansData = FXCollections.observableArrayList();
            myLoansData.setAll(mine);
            applyMyLoansPredicate();
        } catch (Exception e) {
            showError("Errore nell'aggiornamento dei tuoi prestiti: " + e.getMessage());
        }
    }

    // -------------------------
    // Manage Users Tab
    // -------------------------
    public void mostraUtenti() {
        ensureUsersTab();
        tabPane.getSelectionModel().select(usersTab);
        aggiornaUtenti();
    }

    private void ensureUsersTab() {
        if (usersTab == null) {
            usersTab = new Tab("Utenti");
            usersTab.setClosable(true);
            buildUsersView();
            usersTab.setContent(usersRoot);
        }
        if (!tabPane.getTabs().contains(usersTab)) tabPane.getTabs().add(usersTab);
    }

    private void buildUsersView() {
        usersRoot = new BorderPane();
        usersRoot.setPadding(new Insets(10));

        Button btnAdd = new Button("Nuovo");
        Button btnEdit = new Button("Modifica");
        Button btnDelete = new Button("Elimina");
        Button btnCreds = new Button("Crea/Modifica credenziali");
        txtSearchUsers = new TextField();
        txtSearchUsers.setPromptText("Cerca utenti...");
        cmbUserFilter = new ComboBox<>();
        cmbUserFilter.getItems().addAll("Tutti", "Attivi", "Inattivi");
        cmbUserFilter.getSelectionModel().select("Tutti");

        // toolbar: per Admin mostro solo il pulsante credenziali;
        // per Bibliotecario mostro Nuovo/Modifica/Elimina + filtro;
        HBox toolbar;
        if (SessionContext.isAdmin()) {
            toolbar = new HBox(10, btnCreds, new Label("Filtro:"), cmbUserFilter, new Label("Ricerca:"), txtSearchUsers);
        } else {
            toolbar = new HBox(10, btnAdd, btnEdit, btnDelete, new Label("Filtro:"), cmbUserFilter, new Label("Ricerca:"), txtSearchUsers);
        }
        toolbar.setPadding(new Insets(0, 0, 10, 0));
        toolbar.getStyleClass().add("toolbar");
        usersRoot.setTop(toolbar);

        usersTable = new TableView<>();
        usersTable.setPlaceholder(new Label("Nessun utente"));

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
        statoCol.setCellValueFactory(cell -> {
            Utente u = cell.getValue();
            String s = (u == null) ? "" : (u.getDataScadenza() != null && u.getDataScadenza().isBefore(LocalDate.now()) ? "Inattivo" : "Attivo");
            return new ReadOnlyStringWrapper(s);
        });

        usersTable.getColumns().addAll(tesseraCol, nomeCol, cognomeCol, emailCol, telCol, attCol, scadCol, statoCol);

        // Se Admin, mostra anche colonne relative alle credenziali (username e, se disponibile, password_plain)
        if (SessionContext.isAdmin()) {
            TableColumn<Utente, String> usernameCol = new TableColumn<>("Username");
            usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
            TableColumn<Utente, String> passwordCol = new TableColumn<>("Password");
            passwordCol.setCellValueFactory(new PropertyValueFactory<>("password")); // dipende da DAO per popolare
            usersTable.getColumns().addAll(usernameCol, passwordCol);
        }

        usersFiltered = new FilteredList<>(usersData, u -> true);
        usersSorted = new SortedList<>(usersFiltered);
        usersSorted.comparatorProperty().bind(usersTable.comparatorProperty());
        usersTable.setItems(usersSorted);

        txtSearchUsers.textProperty().addListener((obs, o, v) -> applyUsersPredicate());
        cmbUserFilter.valueProperty().addListener((obs, o, v) -> applyUsersPredicate());

        btnAdd.setOnAction(e -> {
            // Solo Bibliotecario può aggiungere utenti (l'Admin poi crea credenziali)
            if (!SessionContext.isBibliotecario()) { showError("Solo il Bibliotecario può creare utenti."); return; }
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
            if (!SessionContext.isBibliotecario() && !SessionContext.isAdmin()) { showError("Non autorizzato."); return; }
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
            if (!SessionContext.isBibliotecario() && !SessionContext.isAdmin()) { showError("Non autorizzato."); return; }
            if (utenteController.elimina(sel.getId())) {
                aggiornaUtenti();
                showInfo("Utente eliminato.");
            } else {
                showError("Impossibile eliminare l'utente.");
            }
        });

        // Bottone importante per Admin: crea/modifica credenziali
        btnCreds.setOnAction(e -> {
            if (!SessionContext.isAdmin()) { showError("Accesso consentito solo all'Admin."); return; }
            Utente sel = usersTable.getSelectionModel().getSelectedItem();
            if (sel == null) { showError("Seleziona un utente per cui creare/modificare le credenziali."); return; }

            String existingUser = sel.getUsername();
            String existingPass = sel.getPassword(); // può essere null se non memorizzata in chiaro

            CredentialsDialog cd = new CredentialsDialog(sel.getId(), existingUser, existingPass);
            cd.showAndWait().ifPresent(pair -> {
                String username = pair.getKey();
                String password = pair.getValue();
                boolean ok;
                try {
                    if (existingUser == null || existingUser.isBlank()) {
                        ok = utenteController.creaCredenziali(sel.getId(), username, password);
                    } else {
                        ok = utenteController.aggiornaCredenziali(sel.getId(), username, password);
                    }
                } catch (Exception ex) {
                    showError("Errore durante la creazione/aggiornamento credenziali: " + ex.getMessage());
                    return;
                }
                if (ok) {
                    aggiornaUtenti();
                    showInfo("Credenziali memorizzate.");
                } else {
                    showError("Operazione fallita (username duplicato o errore DB).");
                }
            });
        });

        usersRoot.setCenter(usersTable);
    }

    private void applyUsersPredicate() {
        if (usersFiltered == null) return;
        String stato = (cmbUserFilter != null && cmbUserFilter.getSelectionModel().getSelectedItem() != null)
                ? cmbUserFilter.getSelectionModel().getSelectedItem() : "Tutti";
        String q = (txtSearchUsers != null && txtSearchUsers.getText() != null)
                ? txtSearchUsers.getText().trim().toLowerCase() : "";
        usersFiltered.setPredicate(makeUserPredicate(q, stato));
    }

    private java.util.function.Predicate<Utente> makeUserPredicate(String q, String statoFilter) {
        return u -> {
            boolean attivo = u.getDataScadenza() == null || !u.getDataScadenza().isBefore(LocalDate.now());
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
            String stato = (u.getDataScadenza() != null && u.getDataScadenza().isBefore(LocalDate.now())) ? "inattivo" : "attivo";

            return tessera.contains(s) || nome.contains(s) || cognome.contains(s) ||
                    email.contains(s) || tel.contains(s) || att.contains(s) || scad.contains(s) ||
                    stato.contains(s);
        };
    }

    private void aggiornaUtenti() {
        try {
            List<Utente> utenti = utenteController.trovaTutti();
            if (usersData == null) usersData = FXCollections.observableArrayList();
            usersData.setAll(utenti);
            applyUsersPredicate();
        } catch (Exception e) {
            showError("Errore nell'aggiornamento degli utenti: " + e.getMessage());
        }
    }

    // -------------------------
    // Profilo Utente
    // -------------------------
    public void mostraProfiloUtente() {
        if (!SessionContext.isUtente()) {
            showError("Profilo disponibile solo per utenti autenticati.");
            return;
        }
        if (profileTab == null) {
            profileTab = new Tab("Profilo");
            profileTab.setClosable(true);
            BorderPane profileRoot = new BorderPane();
            profileRoot.setPadding(new Insets(10));
            profileTab.setContent(profileRoot);
        }
        if (!tabPane.getTabs().contains(profileTab)) tabPane.getTabs().add(profileTab);

        // Carica ed inserisci i dati del profilo
        BorderPane p = (BorderPane) profileTab.getContent();
        Integer tess = SessionContext.getTessera();
        Utente ut = null;
        if (tess != null) {
            List<Utente> all = utenteController.trovaTutti();
            for (Utente u : all) {
                if (u.getTessera() != null && u.getTessera().equals(tess)) { ut = u; break; }
            }
        }
        VBox infoBox = new VBox(8);
        infoBox.setPadding(new Insets(10));
        if (ut != null) {
            infoBox.getChildren().addAll(
                    new Label("Nome: " + safe(ut.getNome())),
                    new Label("Cognome: " + safe(ut.getCognome())),
                    new Label("Tessera: " + (ut.getTessera() != null ? ut.getTessera() : "")),
                    new Label("Data attivazione: " + (ut.getDataAttivazione() != null ? ut.getDataAttivazione() : "")),
                    new Label("Data scadenza: " + (ut.getDataScadenza() != null ? ut.getDataScadenza() : "")),
                    new Label("Email: " + safe(ut.getEmail())),
                    new Label("Telefono: " + safe(ut.getTelefono()))
            );
        } else {
            infoBox.getChildren().add(new Label("Profilo non disponibile."));
        }
        p.setCenter(infoBox);
        tabPane.getSelectionModel().select(profileTab);
    }

    private String safe(String s) { return s != null ? s : ""; }

    // -------------------------
    // UI Helpers
    // -------------------------
    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(null); a.setTitle("Informazione"); a.showAndWait();
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText(null); a.setTitle("Errore"); a.showAndWait();
    }

    // -------------------------
    // Central method to update UI for role changes
    // -------------------------
    private void updateUIForRole() {
        // ricostruisci sidebar
        if (rootContainer != null) {
            rootContainer.setLeft(buildLeftSidebar());
        }
        // aggiorna Home description
        updateHomeDescription();

        // gestione tabs: rimuove quelle non permesse e ne assicura la creazione per quelle permesse
        if (tabPane == null) tabPane = new TabPane();
        // tieni home
        Tab currentHome = homeTab;
        tabPane.getTabs().clear();
        if (currentHome == null) {
            homeTab = new Tab("Home", buildHomeView());
            homeTab.setClosable(false);
            tabPane.getTabs().add(homeTab);
        } else {
            tabPane.getTabs().add(currentHome);
        }

        if (SessionContext.isBibliotecario()) {
            ensureCatalogTab();
            ensureLoansTab();
            ensureUsersTab();
        } else if (SessionContext.isAdmin()) {
            ensureUsersTab();
            // Admin non vede catalogo e prestiti
        } else if (SessionContext.isUtente()) {
            ensureCatalogTab();
            ensureMyLoansTab();
            // create profile tab on-demand
            // disabilita pulsanti di modifica catalogo
            applyCatalogPermissions();
        }

        // ricarica dati
        aggiornaCatalogoLibri();
        aggiornaPrestiti();
        aggiornaUtenti();
        aggiornaMyPrestiti();
    }

    /**
     * Applica il tema corrente (currentTheme) alla root/scene.
     * Può essere chiamato in qualsiasi momento dopo che rootContainer è stato impostato.
     */
    private void applyTheme() {
        if (rootContainer == null) return;

        // rimuovi classi precedenti
        rootContainer.getStyleClass().removeAll(THEME_COLORI_CLASS, THEME_BW_CLASS);
        String themeClass = (currentTheme == Theme.BIANCO_NERO) ? THEME_BW_CLASS : THEME_COLORI_CLASS;
        if (!rootContainer.getStyleClass().contains(themeClass)) {
            rootContainer.getStyleClass().add(themeClass);
        }

        // gestisci stylesheets della Scene (se presente)
        Scene scene = rootContainer.getScene();
        if (scene != null) {
            // rimuovi eventuali references precedenti
            scene.getStylesheets().removeIf(s -> s.endsWith("theme-color.css") || s.endsWith("theme-bw.css"));

            String cssPath = (currentTheme == Theme.BIANCO_NERO) ? THEME_BW_CSS : THEME_COLORI_CSS;
            URL url = getClass().getResource(cssPath);
            if (url != null) {
                String external = url.toExternalForm();
                if (!scene.getStylesheets().contains(external)) {
                    scene.getStylesheets().add(external);
                }
            } else {
                // fallback inline minimale se CSS non trovato
                if (currentTheme == Theme.BIANCO_NERO) {
                    rootContainer.setStyle("-fx-accent: #000000; -fx-focus-color: #000000; -fx-base: #f2f2f2;");
                } else {
                    rootContainer.setStyle("-fx-accent: #3f51b5; -fx-focus-color: #3f51b5; -fx-base: #f6f7fb;");
                }
            }
        } else {
            // se scene non è ancora disponibile, applica il CSS alla root stessa
            String cssPath = (currentTheme == Theme.BIANCO_NERO) ? THEME_BW_CSS : THEME_COLORI_CSS;
            URL url = getClass().getResource(cssPath);
            if (url != null) {
                String external = url.toExternalForm();
                if (!rootContainer.getStylesheets().contains(external)) {
                    rootContainer.getStylesheets().add(external);
                }
            }
        }
    }

    public void mostraHome() {
        if (tabPane != null && homeTab != null) {
            tabPane.getSelectionModel().select(homeTab);
        }
    }
}
