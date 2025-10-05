package it.biblioteca.ui;

import it.biblioteca.controller.PrestitoController;
import it.biblioteca.entity.Book;
import it.biblioteca.entity.Prestito;
import it.biblioteca.entity.Utente;
import it.biblioteca.security.AuthService;
import it.biblioteca.security.SessionContext;
import it.biblioteca.events.EventBus;
import it.biblioteca.events.Subscription;
import it.biblioteca.events.events.BookChanged;
import it.biblioteca.events.events.PrestitoChanged;
import it.biblioteca.events.events.UtenteChanged;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
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

import it.biblioteca.ui.facade.UiFacade;

public class ContentManager {

    public enum Theme { COLORI, BIANCO_NERO }

    private Subscription subBook;
    private Subscription subLoan;
    private Subscription subUser;

    private static final String THEME_COLORI_CSS = "/css/theme-color.css";
    private static final String THEME_BW_CSS     = "/css/theme-bw.css";
    private static final String THEME_COLORI_CLASS = "theme-color";
    private static final String THEME_BW_CLASS     = "theme-bw";
    private static final String RICERCA = "Ricerca:";
    private static final String TOOLBAR = "toolbar";
    private static final String OP_BIBLIOTECARIO = "Operazione consentita solo al Bibliotecario.";
    private static final String ATTIVI = "Attivi";
    private static final String TUTTI = "Tutti";
    private static final String FILTRO = "Filtro:";

    private final UiFacade ui;

    private BorderPane rootContainer;
    private TabPane tabPane;
    private Tab homeTab;
    private Tab catalogTab;
    private Tab loansTab;
    private Tab usersTab;
    private Tab profileTab;
    private Tab myLoansTab;

    private Theme currentTheme = Theme.COLORI;

    private TableView<Book> catalogTable;
    private ObservableList<Book> catalogData;
    private FilteredList<Book> catalogFiltered;
    private SortedList<Book> catalogSorted;
    private TextField txtSearchCatalog;
    private BorderPane catalogRoot;

    private Button btnAddBook;
    private Button btnEditBook;
    private Button btnRemoveBook;

    private TableView<Prestito> loansTable;
    private ObservableList<Prestito> loansData;
    private FilteredList<Prestito> loansFiltered;
    private SortedList<Prestito> loansSorted;
    private TextField txtSearchLoans;
    private ComboBox<String> cmbLoanFilter;
    private BorderPane loansRoot;

    private TableView<Prestito> myLoansTable;
    private ObservableList<Prestito> myLoansData;
    private FilteredList<Prestito> myLoansFiltered;
    private SortedList<Prestito> myLoansSorted;
    private TextField txtSearchMyLoans;
    private ComboBox<String> cmbMyLoanFilter;
    private BorderPane myLoansRoot;

    private TableView<Utente> usersTable;
    private ObservableList<Utente> usersData;
    private FilteredList<Utente> usersFiltered;
    private SortedList<Utente> usersSorted;
    private TextField txtSearchUsers;
    private ComboBox<String> cmbUserFilter;
    private BorderPane usersRoot;

    public ContentManager(UiFacade ui) {
        this.ui = ui;
        this.catalogData = FXCollections.observableArrayList();
        this.loansData = FXCollections.observableArrayList();
        this.myLoansData = FXCollections.observableArrayList();
        this.usersData = FXCollections.observableArrayList();
    }

    public void inizializzaContenuto(BorderPane root) {
        this.rootContainer = root;

        while (true) {
            StartupDialog dlg = new StartupDialog();
            Optional<StartupResult> res = dlg.showAndWait();

            if (res.isEmpty()) {
                Platform.exit();
                return;
            }
            StartupResult r = res.get();
            if (!r.isValid()) {
                showError("Compila tutti i campi richiesti.");
                continue;
            }

            boolean okDb = it.biblioteca.db.DatabaseConfig.testCredentials(r.getUsername(), r.getPassword());
            if (!okDb) {
                showError("Credenziali DB non valide. Riprova.");
                continue;
            }

            it.biblioteca.db.DatabaseConfig.apply(r);

            this.currentTheme = r.getTheme();
            applyTheme();

            AuthService.AuthResult ar = AuthService.authenticate(r.getAppUsername(), r.getAppPassword());
            if (!ar.ok()) {
                showError("Credenziali applicative non valide. Riprova.");
                continue;
            }

            SessionContext.setRole(ar.role());
            SessionContext.setUserId(ar.userId());
            SessionContext.setTessera(ar.tessera());

            break;
        }

        tabPane = new TabPane();
        subscribeToEvents();
        homeTab = new Tab("Home", buildHomeView());
        homeTab.setClosable(false);
        tabPane.getTabs().add(homeTab);

        VBox leftBar = buildLeftSidebar();
        root.setLeft(leftBar);

        root.setCenter(tabPane);

        aggiornaCatalogoLibri();
        aggiornaPrestiti();
        aggiornaUtenti();
        aggiornaMieiPrestiti();
    }

    private void subscribeToEvents() {
        EventBus bus = EventBus.getDefault();
        subBook = bus.subscribe(BookChanged.class, e -> Platform.runLater(this::aggiornaCatalogoLibri));
        subLoan = bus.subscribe(PrestitoChanged.class, e -> Platform.runLater(this::aggiornaPrestiti));
        subUser = bus.subscribe(UtenteChanged.class, e -> Platform.runLater(this::aggiornaUtenti));
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

        if (SessionContext.isAdmin()) {
            left.getChildren().addAll(btnHome, btnUsers, btnExit);
        } else if (SessionContext.isBibliotecario()) {
            left.getChildren().addAll(btnHome, btnCatalog, btnLoans, btnUsers, btnExit);
        } else if (SessionContext.isUtente()) {
            left.getChildren().addAll(btnHome, btnCatalog, btnProfile, btnMyLoans, btnExit);
        } else {
            left.getChildren().addAll(btnHome, btnExit);
        }

        return left;
    }

    private BorderPane buildHomeView() {
        BorderPane p = new BorderPane();
        p.setPadding(new Insets(20));
        Label title = new Label("Benvenuto nella Biblioteca");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label info = new Label(getHomeDescriptionForRole());
        info.setWrapText(true);

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER);

        Button btnGoCatalog = new Button("Apri Catalogo");
        btnGoCatalog.setOnAction(e -> mostraCatalogoLibri());

        Button btnGoPrestiti = new Button("Apri Prestiti");
        btnGoPrestiti.setOnAction(e -> mostraPrestiti());

        Button btnGoUsers = new Button("Apri Utenti");
        btnGoUsers.setOnAction(e -> mostraUtenti());

        Button btnGoProfile = new Button("Apri profilo");
        btnGoProfile.setOnAction(e -> mostraProfiloUtente());

        Button btnGoMyLoans = new Button("Apri i miei prestiti");
        btnGoMyLoans.setOnAction(e -> mostraMieiPrestiti());

        Button btnExit = new Button("Esci");
        btnExit.setOnAction(e -> Platform.exit());

        if (SessionContext.isAdmin()) {
            actions.getChildren().addAll(btnGoUsers, btnExit);
        } else if (SessionContext.isBibliotecario()) {
            actions.getChildren().addAll(btnGoCatalog, btnGoPrestiti, btnGoUsers, btnExit);
        } else if (SessionContext.isUtente()) {
            actions.getChildren().addAll(btnGoCatalog, btnGoProfile, btnGoMyLoans, btnExit);
        } else {
            actions.getChildren().addAll(btnExit);
        }

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
            return "Sei connesso come Utente. Puoi visualizzare il catalogo (solo copie disponibili), visualizzare i tuoi dati personali e consultare i tuoi prestiti (in corso e passati).";
        } else {
            return "Effettua il login per continuare.";
        }
    }

    private void updateHomeDescription() {
        if (homeTab != null && homeTab.getContent() instanceof BorderPane) {
            homeTab.setContent(buildHomeView());
        }
    }

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
        HBox toolbar = new HBox(10, btnAddBook, btnEditBook, btnRemoveBook, new Label(RICERCA), txtSearchCatalog);
        toolbar.setPadding(new Insets(0, 0, 10, 0));
        toolbar.getStyleClass().add(TOOLBAR);

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

        TableColumn<Book, Integer> copieCol = new TableColumn<>("Copie");
        copieCol.setCellValueFactory(cell -> {
            Book b = cell.getValue();
            int copies = 0;
            if (b != null) {
                copies = b.getCopie();
            }
            return new ReadOnlyObjectWrapper<>(copies);
        });

        TableColumn<Book, Integer> copieDispCol = new TableColumn<>("Copie disponibili");
        copieDispCol.setCellValueFactory(cell -> {
            Book b = cell.getValue();
            int copies = 0;
            if (b != null) copies = b.getCopie();
            long active = 0L;
            try {
                if (b != null && b.getId() != null) {
                    List<Prestito> attivi = ui.listActiveLoans();
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

        if (SessionContext.isUtente()) {
            catalogTable.getColumns().addAll(isbnCol, titoloCol, autoreCol, dataCol, editoreCol, copieDispCol);
        } else {
            catalogTable.getColumns().addAll(isbnCol, titoloCol, autoreCol, dataCol, editoreCol, copieCol, copieDispCol);
        }

        catalogTable.setItems(catalogSorted);

        txtSearchCatalog.textProperty().addListener((obs, o, val) -> {
            String q = val == null ? "" : val.trim().toLowerCase();
            catalogFiltered.setPredicate(makeBookPredicate(q));
        });

        btnAddBook.setOnAction(e -> {
            if (!SessionContext.isBibliotecario()) { showError(OP_BIBLIOTECARIO); return; }
            AddBookDialog dialog = new AddBookDialog();
            dialog.showAndWait().ifPresent(bean -> {
                boolean ok = ui.addBook(bean);
                if (ok) { aggiornaCatalogoLibri(); showInfo("Libro aggiunto."); }
                else showError("Impossibile aggiungere il libro.");
            });
        });

        btnEditBook.setOnAction(e -> {
            if (!SessionContext.isBibliotecario()) { showError(OP_BIBLIOTECARIO); return; }
            Book selected = catalogTable.getSelectionModel().getSelectedItem();
            if (selected == null) { showError("Seleziona un libro da modificare."); return; }
            EditBookDialog dialog = new EditBookDialog(selected);
            dialog.showAndWait().ifPresent(bean -> {
                boolean ok = ui.updateBook(bean);
                if (ok) { aggiornaCatalogoLibri(); showInfo("Libro aggiornato."); }
                else showError("Impossibile aggiornare il libro.");
            });
        });

        btnRemoveBook.setOnAction(e -> {
            if (!SessionContext.isBibliotecario()) { showError(OP_BIBLIOTECARIO); return; }
            Book selected = catalogTable.getSelectionModel().getSelectedItem();
            if (selected == null) { showError("Seleziona un libro da rimuovere."); return; }
            boolean ok = ui.removeBook(selected.getId());
            if (ok) { aggiornaCatalogoLibri(); showInfo("Libro rimosso dal database."); }
            else showError("Impossibile rimuovere il libro. Verifica che non abbia prestiti attivi o prenotazioni.");
        });

        catalogRoot.setTop(toolbar);
        catalogRoot.setCenter(catalogTable);

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
            List<Book> libri = ui.listBooks();
            if (catalogData == null) catalogData = FXCollections.observableArrayList();
            catalogData.setAll(libri);
        } catch (Exception e) {
            showError("Errore nell'aggiornamento del catalogo: " + e.getMessage());
        }
    }

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
        cmbLoanFilter.getItems().addAll(TUTTI, ATTIVI, "Non attivi");
        cmbLoanFilter.getSelectionModel().select(TUTTI);
        txtSearchLoans = new TextField();
        txtSearchLoans.setPromptText("Cerca nei prestiti...");

        HBox toolbar = new HBox(10, btnAddLoan, btnReturn, btnRefresh,
                new Label(FILTRO), cmbLoanFilter,
                new Label(RICERCA), txtSearchLoans);
        toolbar.setPadding(new Insets(0, 0, 10, 0));
        toolbar.getStyleClass().add(TOOLBAR);

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
            if (!SessionContext.isBibliotecario()) { showError(OP_BIBLIOTECARIO); return; }

            List<Book> tutti = ui.listBooks();
            Map<Long, Long> attiviPerLibro = ui.listActiveLoans().stream()
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
                SelectUserDialog selectUser = new SelectUserDialog(ui.users(), this::aggiornaUtenti);
                selectUser.showAndWait().ifPresent(selectedUser -> {
                    PrestitoDialog dlg = new PrestitoDialog(selectedBook, selectedUser);
                    dlg.showAndWait().ifPresent(bean -> {
                        PrestitoController.Esito esito = ui.registerLoan(bean);
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
            if (!SessionContext.isBibliotecario()) { showError(OP_BIBLIOTECARIO); return; }
            Prestito sel = loansTable.getSelectionModel().getSelectedItem();
            if (sel == null) { showError("Seleziona un prestito da chiudere."); return; }
            boolean ok = ui.registerReturn(sel.getId(), LocalDate.now());
            if (ok) { aggiornaPrestiti(); aggiornaCatalogoLibri(); showInfo("Restituzione registrata."); }
            else showError("Impossibile registrare la restituzione.");
        });

        btnRefresh.setOnAction(e -> aggiornaPrestiti());

        loansRoot.setTop(toolbar);
        loansRoot.setCenter(loansTable);

        if (!SessionContext.isBibliotecario()) {
            loansTab.setDisable(true);
        }
    }

    private void applyLoansPredicate() {
        if (loansFiltered == null) return;
        String filter = cmbLoanFilter != null ? cmbLoanFilter.getSelectionModel().getSelectedItem() : TUTTI;
        String q = (txtSearchLoans != null && txtSearchLoans.getText() != null) ? txtSearchLoans.getText().trim().toLowerCase() : "";

        loansFiltered.setPredicate(p -> {
            boolean isActive = p.getDataRestituzione() == null;
            boolean passFilter = TUTTI.equals(filter) ||
                    (ATTIVI.equals(filter) && isActive) ||
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
            List<Prestito> prestiti = ui.listLoans();
            if (loansData == null) loansData = FXCollections.observableArrayList();
            loansData.setAll(prestiti);
            applyLoansPredicate();
        } catch (Exception e) {
            showError("Errore nell'aggiornamento dei prestiti: " + e.getMessage());
        }
    }

    public void mostraMieiPrestiti() {
        ensureMyLoansTab();
        tabPane.getSelectionModel().select(myLoansTab);
        aggiornaMieiPrestiti();
    }

    private void buildMyLoansView() {
        myLoansRoot = new BorderPane();
        myLoansRoot.setPadding(new Insets(10));

        Button btnRefresh = new Button("Aggiorna");
        cmbMyLoanFilter = new ComboBox<>();
        cmbMyLoanFilter.getItems().addAll(TUTTI, "In corso", "Conclusi");
        cmbMyLoanFilter.getSelectionModel().select(TUTTI);
        txtSearchMyLoans = new TextField();
        txtSearchMyLoans.setPromptText("Cerca nei miei prestiti...");

        HBox toolbar = new HBox(10, btnRefresh, new Label(FILTRO), cmbMyLoanFilter, new Label(RICERCA), txtSearchMyLoans);
        toolbar.setPadding(new Insets(0, 0, 10, 0));
        toolbar.getStyleClass().add(TOOLBAR);

        myLoansTable = new TableView<>();
        myLoansTable.setPlaceholder(new Label("Nessun prestito da mostrare"));

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

        cmbMyLoanFilter.valueProperty().addListener((obs, o, v) -> applyMyLoansPredicate());
        txtSearchMyLoans.textProperty().addListener((obs, o, v) -> applyMyLoansPredicate());

        btnRefresh.setOnAction(e -> aggiornaMieiPrestiti());

        myLoansRoot.setTop(toolbar);
        myLoansRoot.setCenter(myLoansTable);
    }

    private void applyMyLoansPredicate() {
        if (myLoansFiltered == null) return;
        String filter = cmbMyLoanFilter != null ? cmbMyLoanFilter.getSelectionModel().getSelectedItem() : TUTTI;
        String q = (txtSearchMyLoans != null && txtSearchMyLoans.getText() != null) ? txtSearchMyLoans.getText().trim().toLowerCase() : "";

        myLoansFiltered.setPredicate(p -> {
            boolean isActive = p.getDataRestituzione() == null;
            boolean passFilter = TUTTI.equals(filter) ||
                    ("In corso".equals(filter) && isActive) ||
                    ("Conclusi".equals(filter) && !isActive);
            if (!passFilter) return false;

            if (q.isBlank()) return true;

            String idStr = p.getId() != null ? String.valueOf(p.getId()) : "";
            String libro = p.getLibroTitoloSnapshot() != null ? p.getLibroTitoloSnapshot().toLowerCase() : "";
            String dp = p.getDataPrestito() != null ? p.getDataPrestito().toString() : "";
            String dr = p.getDataRestituzione() != null ? p.getDataRestituzione().toString() : "";

            return idStr.contains(q) || libro.contains(q) || dp.contains(q) || dr.contains(q);
        });
    }

    private void aggiornaMieiPrestiti() {
        try {
            Integer tess = SessionContext.getTessera();
            Long utenteId = null;
            if (tess != null) {
                List<Utente> all = ui.listUsers();
                for (Utente u : all) {
                    if (u.getTessera() != null && u.getTessera().equals(tess)) {
                        utenteId = u.getId();
                        break;
                    }
                }
            }

            if (utenteId == null) {
                if (myLoansData == null) myLoansData = FXCollections.observableArrayList();
                myLoansData.clear();
                applyMyLoansPredicate();
                return;
            }

            List<Prestito> all = ui.listLoans();
            Long finalUtenteId = utenteId;
            List<Prestito> miei = all.stream()
                    .filter(p -> p.getUtenteId() != null && p.getUtenteId().equals(finalUtenteId))
                    .collect(Collectors.toList());

            if (myLoansData == null) myLoansData = FXCollections.observableArrayList();
            myLoansData.setAll(miei);
            applyMyLoansPredicate();
        } catch (Exception e) {
            showError("Errore nell'aggiornamento dei tuoi prestiti: " + e.getMessage());
        }
    }

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
        Button btnCred = new Button("Crea/Modifica credenziali");

        txtSearchUsers = new TextField();
        txtSearchUsers.setPromptText("Cerca utenti...");
        cmbUserFilter = new ComboBox<>();
        cmbUserFilter.getItems().addAll(TUTTI, ATTIVI, "Inattivi");
        cmbUserFilter.getSelectionModel().select(TUTTI);

        HBox toolbar = new HBox(10, btnAdd, btnEdit, btnDelete, btnCred, new Label(FILTRO), cmbUserFilter, new Label(RICERCA), txtSearchUsers);
        toolbar.setPadding(new Insets(0, 0, 10, 0));
        toolbar.getStyleClass().add(TOOLBAR);
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

        if (SessionContext.isAdmin()) {
            TableColumn<Utente, String> usernameCol = new TableColumn<>("Username");
            usernameCol.setCellValueFactory(cell -> {
                Utente u = cell.getValue();
                String username = "";
                try {
                    if (u != null && u.getId() != null) {
                        username = ui.getUsernameForUserId(u.getId()).orElse("");
                    }
                } catch (Exception ex) {
                    username = "";
                }
                return new ReadOnlyStringWrapper(username);
            });

            TableColumn<Utente, String> passwordCol = new TableColumn<>("Password");
            passwordCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper("*****"));

            usersTable.getColumns().addAll(usernameCol, passwordCol);
        }

        usersFiltered = new FilteredList<>(usersData, u -> true);
        usersSorted = new SortedList<>(usersFiltered);
        usersSorted.comparatorProperty().bind(usersTable.comparatorProperty());
        usersTable.setItems(usersSorted);

        txtSearchUsers.textProperty().addListener((obs, o, v) -> applyUsersPredicate());
        cmbUserFilter.valueProperty().addListener((obs, o, v) -> applyUsersPredicate());

        btnAdd.setOnAction(e -> {
            if (!SessionContext.isBibliotecario()) { showError("Solo il Bibliotecario può creare utenti."); return; }
            AddEditUserDialog dlg = new AddEditUserDialog(ui.users(), null);
            dlg.showAndWait().ifPresent(bean -> {
                if (ui.addUser(bean)) {
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
            AddEditUserDialog dlg = new AddEditUserDialog(ui.users(), sel);
            dlg.showAndWait().ifPresent(bean -> {
                bean.setId(sel.getId());
                if (ui.updateUser(bean)) {
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
            if (ui.deleteUser(sel.getId())) {
                aggiornaUtenti();
                showInfo("Utente eliminato.");
            } else {
                showError("Impossibile eliminare l'utente.");
            }
        });

        btnCred.setOnAction(e -> {
            if (!SessionContext.isAdmin()) { showError("Solo Admin può gestire le credenziali."); return; }
            Utente sel = usersTable.getSelectionModel().getSelectedItem();
            if (sel == null) { showError("Seleziona un utente per associare le credenziali."); return; }

            Optional<String> existing;
            try {
                existing = ui.getUsernameForUserId(sel.getId());
            } catch (Exception ex) {
                existing = Optional.empty();
            }
            final String existingUsername = existing.orElse("");

            CredentialsDialog dlg = new CredentialsDialog(existingUsername, null);
            Optional<String> finalExisting = existing;
            dlg.showAndWait().ifPresent(pair -> {
                String username = pair.getKey();
                String password = pair.getValue();
                boolean ok;
                try {
                    if (finalExisting.isPresent() && !existingUsername.isBlank()) {
                        ok = ui.updateCredentials(sel.getId(), username, password);
                    } else {
                        ok = ui.createCredentials(sel.getId(), username, password);
                    }
                    if (ok) {
                        aggiornaUtenti();
                        showInfo("Credenziali salvate.");
                    } else {
                        showError("Impossibile salvare credenziali.");
                    }
                } catch (Exception ex) {
                    showError("Errore durante salvataggio credenziali: " + ex.getMessage());
                }
            });
        });

        usersRoot.setCenter(usersTable);
    }

    private void applyUsersPredicate() {
        if (usersFiltered == null) return;
        String stato = (cmbUserFilter != null && cmbUserFilter.getSelectionModel().getSelectedItem() != null)
                ? cmbUserFilter.getSelectionModel().getSelectedItem() : TUTTI;
        String q = (txtSearchUsers != null && txtSearchUsers.getText() != null)
                ? txtSearchUsers.getText().trim().toLowerCase() : "";
        usersFiltered.setPredicate(makeUserPredicate(q, stato));
    }

    private java.util.function.Predicate<Utente> makeUserPredicate(String q, String statoFilter) {
        return u -> {
            boolean attivo = u.getDataScadenza() == null || !u.getDataScadenza().isBefore(LocalDate.now());
            boolean statoOk = TUTTI.equals(statoFilter) ||
                    (ATTIVI.equals(statoFilter) && attivo) ||
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
            List<Utente> utenti = ui.listUsers();
            if (usersData == null) usersData = FXCollections.observableArrayList();
            usersData.setAll(utenti);
            applyUsersPredicate();
        } catch (Exception e) {
            showError("Errore nell'aggiornamento degli utenti: " + e.getMessage());
        }
    }

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

        BorderPane p = (BorderPane) profileTab.getContent();
        Integer tess = SessionContext.getTessera();
        Utente ut = null;
        if (tess != null) {
            List<Utente> all = ui.listUsers();
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

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(null); a.setTitle("Informazione"); a.showAndWait();
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText(null); a.setTitle("Errore"); a.showAndWait();
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

    private void applyTheme() {
        if (rootContainer == null) return;

        rootContainer.getStyleClass().removeAll(THEME_COLORI_CLASS, THEME_BW_CLASS);
        String themeClass = (currentTheme == Theme.BIANCO_NERO) ? THEME_BW_CLASS : THEME_COLORI_CLASS;
        if (!rootContainer.getStyleClass().contains(themeClass)) {
            rootContainer.getStyleClass().add(themeClass);
        }

        Scene scene = rootContainer.getScene();
        if (scene != null) {
            scene.getStylesheets().removeIf(s -> s.endsWith("theme-color.css") || s.endsWith("theme-bw.css"));

            String cssPath = (currentTheme == Theme.BIANCO_NERO) ? THEME_BW_CSS : THEME_COLORI_CSS;
            URL url = getClass().getResource(cssPath);
            if (url != null) {
                String external = url.toExternalForm();
                if (!scene.getStylesheets().contains(external)) {
                    scene.getStylesheets().add(external);
                }
            } else {
                if (currentTheme == Theme.BIANCO_NERO) {
                    rootContainer.setStyle("-fx-accent: #000000; -fx-focus-color: #000000; -fx-base: #f2f2f2;");
                } else {
                    rootContainer.setStyle("-fx-accent: #3f51b5; -fx-focus-color: #3f51b5; -fx-base: #f6f7fb;");
                }
            }
        } else {
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