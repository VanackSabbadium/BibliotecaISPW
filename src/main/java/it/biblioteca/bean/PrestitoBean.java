package it.biblioteca.bean;

import java.time.LocalDate;

public class PrestitoBean {
    private Long id;

    private Long libroId;
    private Long utenteId;

    private String utenteSnapshot;
    private String libroTitoloSnapshot;

    private LocalDate dataPrestito;

    public PrestitoBean() { // Constructor (non fa nulla)
        }

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
}