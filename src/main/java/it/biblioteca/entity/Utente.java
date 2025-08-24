package it.biblioteca.entity;

import java.time.LocalDate;

public class Utente {
    private Long id;
    private Integer tessera;
    private String nome;
    private String cognome;
    private String email;
    private String telefono;

    private LocalDate dataAttivazione; // nuova
    private LocalDate dataScadenza;    // nuova

    public Utente() {}

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

    // Calcolato: attivo se oggi è tra dataAttivazione e dataScadenza (estremi inclusi).
    public boolean isAttivo() {
        LocalDate today = LocalDate.now();
        if (dataAttivazione != null && today.isBefore(dataAttivazione)) return false;
        if (dataScadenza != null && today.isAfter(dataScadenza)) return false;
        return true;
    }

    // Utile per la colonna "Stato" nella tabella
    public String getStato() {
        return isAttivo() ? "Attivo" : "Inattivo";
    }
}