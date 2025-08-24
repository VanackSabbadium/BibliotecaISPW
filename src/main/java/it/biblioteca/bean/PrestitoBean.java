package it.biblioteca.bean;

import java.time.LocalDate;

/**
 * Bean di trasferimento dati per operazioni sui prestiti.
 * Utilizzato da controller/DAO per inserire e chiudere prestiti.
 */

public class PrestitoBean {
    private Long id;

    // Riferimenti
    private Long libroId;
    private Long utenteId;

    // Snapshot memorizzati in tabella "prestiti" per storicità
    private String utenteSnapshot;         // es: "Mario Rossi"
    private String libroTitoloSnapshot;    // es: "Il Nome della Rosa"

    // Date
    private LocalDate dataPrestito;
    // private LocalDate dataRestituzione;

    public PrestitoBean() {}

    // Getters/Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLibroId() {
        return libroId;
    }

    public void setLibroId(Long libroId) {
        this.libroId = libroId;
    }

    public Long getUtenteId() {
        return utenteId;
    }

    public void setUtenteId(Long utenteId) {
        this.utenteId = utenteId;
    }

    public String getUtenteSnapshot() {
        return utenteSnapshot;
    }

    public void setUtenteSnapshot(String utenteSnapshot) {
        this.utenteSnapshot = utenteSnapshot;
    }

    public String getLibroTitoloSnapshot() {
        return libroTitoloSnapshot;
    }

    public void setLibroTitoloSnapshot(String libroTitoloSnapshot) {
        this.libroTitoloSnapshot = libroTitoloSnapshot;
    }

    public LocalDate getDataPrestito() {
        return dataPrestito;
    }

    public void setDataPrestito(LocalDate dataPrestito) {
        this.dataPrestito = dataPrestito;
    }

    /*
    public LocalDate getDataRestituzione() {
        return dataRestituzione;
    }

    public void setDataRestituzione(LocalDate dataRestituzione) {
        this.dataRestituzione = dataRestituzione;
    }
    */
}