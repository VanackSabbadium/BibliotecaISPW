// src/main/java/it/biblioteca/entity/Book.java
package it.biblioteca.entity;

import java.time.LocalDate;

public class Book {
    private Long id;
    private String isbn;
    private String titolo;
    private String autore;
    private LocalDate dataPubblicazione;
    private String casaEditrice;
    private boolean attivo = true; // può restare; ora cancelliamo fisicamente
    private int copie = 1;

    public Book() {}

    public Book(Long id, String isbn, String titolo, String autore,
                LocalDate dataPubblicazione, String casaEditrice, boolean attivo, int copie) {
        this.id = id;
        this.isbn = isbn;
        this.titolo = titolo;
        this.autore = autore;
        this.dataPubblicazione = dataPubblicazione;
        this.casaEditrice = casaEditrice;
        this.attivo = attivo;
        this.copie = copie;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getTitolo() { return titolo; }
    public void setTitolo(String titolo) { this.titolo = titolo; }

    public String getAutore() { return autore; }
    public void setAutore(String autore) { this.autore = autore; }

    public LocalDate getDataPubblicazione() { return dataPubblicazione; }
    public void setDataPubblicazione(LocalDate dataPubblicazione) { this.dataPubblicazione = dataPubblicazione; }

    public String getCasaEditrice() { return casaEditrice; }
    public void setCasaEditrice(String casaEditrice) { this.casaEditrice = casaEditrice; }

    public boolean isAttivo() { return attivo; }
    public void setAttivo(boolean attivo) { this.attivo = attivo; }

    public int getCopie() { return copie; }
    public void setCopie(int copie) { this.copie = copie; }
}