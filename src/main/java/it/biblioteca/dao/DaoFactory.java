package it.biblioteca.dao;

public interface DaoFactory {
    BookDAO bookDAO();
    UtenteDAO utenteDAO();
    PrestitoDAO prestitoDAO();
}
