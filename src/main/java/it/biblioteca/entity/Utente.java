package it.biblioteca.entity;

import java.time.LocalDate;

public class Utente {
    private Long id;
    private Integer tessera;
    private String nome;
    private String cognome;
    private String email;
    private String telefono;
    private LocalDate dataAttivazione;
    private LocalDate dataScadenza;

    private String username;

    public Utente() { // Constructor (non fa nulla)
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getTessera() { return tessera; }
    public void setTessera(Integer tessera) { this.tessera = tessera; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCognome() { return cognome; }
    public void setCognome(String cognome) { this.cognome = cognome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public LocalDate getDataAttivazione() { return dataAttivazione; }
    public void setDataAttivazione(LocalDate dataAttivazione) { this.dataAttivazione = dataAttivazione; }

    public LocalDate getDataScadenza() { return dataScadenza; }
    public void setDataScadenza(LocalDate dataScadenza) { this.dataScadenza = dataScadenza; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}