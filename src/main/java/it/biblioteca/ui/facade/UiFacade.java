package it.biblioteca.ui.facade;

import it.biblioteca.bean.BookBean;
import it.biblioteca.bean.PrestitoBean;
import it.biblioteca.bean.UtenteBean;
import it.biblioteca.controller.BookController;
import it.biblioteca.controller.PrestitoController;
import it.biblioteca.controller.UtenteController;
import it.biblioteca.entity.Book;
import it.biblioteca.entity.Prestito;
import it.biblioteca.entity.Utente;
import it.biblioteca.service.AppContext;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class UiFacade {
    private final BookController bookController;
    private final PrestitoController prestitoController;
    private final UtenteController utenteController;

    public UiFacade(BookController bookController, PrestitoController prestitoController, UtenteController utenteController) {
        this.bookController = bookController;
        this.prestitoController = prestitoController;
        this.utenteController = utenteController;
    }

    public static UiFacade from(AppContext ctx) {
        return new UiFacade(
                new BookController(ctx.books()),
                new PrestitoController(ctx.loans()),
                new UtenteController(ctx.users())
        );
    }

    public List<Book> listBooks() { return bookController.trovaTutti(); }
    public boolean addBook(BookBean bean) { return bookController.aggiungiLibro(bean); }
    public boolean updateBook(BookBean bean) { return bookController.aggiornaLibro(bean); }
    public boolean removeBook(Long id) { return bookController.rimuoviLibro(id); }

    public List<Prestito> listLoans() { return prestitoController.trovaTutti(); }
    public List<Prestito> listActiveLoans() { return prestitoController.trovaPrestitiAttivi(); }
    public PrestitoController.Esito registerLoan(PrestitoBean bean) { return prestitoController.registraPrestito(bean); }
    public boolean registerReturn(Long prestitoId, LocalDate data) { return prestitoController.registraRestituzione(prestitoId, data); }

    public List<Utente> listUsers() { return utenteController.trovaTutti(); }
    public List<Utente> listActiveUsers() { return utenteController.trovaAttivi(); }
    public boolean addUser(UtenteBean bean) { return utenteController.aggiungi(bean); }
    public boolean updateUser(UtenteBean bean) { return utenteController.aggiorna(bean); }
    public boolean deleteUser(Long id) { return utenteController.elimina(id); }
    public boolean createCredentials(Long utenteId, String username, String passwordPlain) { return utenteController.creaCredenziali(utenteId, username, passwordPlain); }
    public boolean updateCredentials(Long utenteId, String username, String passwordPlain) { return utenteController.aggiornaCredenziali(utenteId, username, passwordPlain); }
    public Optional<String> getUsernameForUserId(Long utenteId) { return utenteController.getUsernameForUserId(utenteId); }

    public BookController books() { return bookController; }
    public PrestitoController loans() { return prestitoController; }
    public UtenteController users() { return utenteController; }
}