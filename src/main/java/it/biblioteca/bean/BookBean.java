package it.biblioteca.bean;

import java.time.LocalDate;

public class BookBean {
    private Long id;
    private String isbn;
    private String titolo;
    private String autore;
    private LocalDate dataPubblicazione;
    private String casaEditrice;
    private int copie = 1;

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

    public int getCopie() { return copie; }
    public void setCopie(int copie) { this.copie = copie; }
}