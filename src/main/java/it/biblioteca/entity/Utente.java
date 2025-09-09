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

    // campi aggiunti per mostrare username/password nella UI (Admin)
    private String username;
    private String password; // password in chiaro (se presente in DB) — attenzione alla sicurezza

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

    // --- nuovi getter/setter per username/password (popolati dal DAO con LEFT JOIN su credenziali) ---
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    /**
     * NOTA: questo campo contiene la password in chiaro se la colonna password_plain è presente
     * e valorizzata nel DB. Per ragioni di sicurezza valutare di non salvare password in chiaro.
     */
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
