package it.biblioteca.entity;

import java.time.LocalDate;

public class Prestito {
    private Long id;
    private Long libroId;
    private Long utenteId;
    private String utente;
    private LocalDate dataPrestito;
    private LocalDate dataRestituzione;

    private String libroTitoloSnapshot;

    public Prestito() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getLibroId() { return libroId; }
    public void setLibroId(Long libroId) { this.libroId = libroId; }

    public Long getUtenteId() { return utenteId; }
    public void setUtenteId(Long utenteId) { this.utenteId = utenteId; }

    public String getUtente() { return utente; }
    public void setUtente(String utente) { this.utente = utente; }

    public LocalDate getDataPrestito() { return dataPrestito; }
    public void setDataPrestito(LocalDate dataPrestito) { this.dataPrestito = dataPrestito; }

    public LocalDate getDataRestituzione() { return dataRestituzione; }
    public void setDataRestituzione(LocalDate dataRestituzione) { this.dataRestituzione = dataRestituzione; }


    public String getLibroTitoloSnapshot() { return libroTitoloSnapshot; }
    public void setLibroTitoloSnapshot(String libroTitoloSnapshot) { this.libroTitoloSnapshot = libroTitoloSnapshot; }
}