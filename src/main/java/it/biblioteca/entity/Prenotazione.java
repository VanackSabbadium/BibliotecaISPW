package it.biblioteca.entity;

import java.time.LocalDate;

public class Prenotazione {
    private Long id;
    private Long libroId;
    private Long utenteId;
    private LocalDate dataPrenotazione;
    private LocalDate dataEvasione; // null se ancora attiva
    private String libroTitoloSnapshot;
    private String utenteSnapshot;

    public Prenotazione() {}

    public Prenotazione(Long id, Long libroId, Long utenteId, LocalDate dataPrenotazione, LocalDate dataEvasione,
                        String libroTitoloSnapshot, String utenteSnapshot) {
        this.id = id;
        this.libroId = libroId;
        this.utenteId = utenteId;
        this.dataPrenotazione = dataPrenotazione;
        this.dataEvasione = dataEvasione;
        this.libroTitoloSnapshot = libroTitoloSnapshot;
        this.utenteSnapshot = utenteSnapshot;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getLibroId() { return libroId; }
    public void setLibroId(Long libroId) { this.libroId = libroId; }

    public Long getUtenteId() { return utenteId; }
    public void setUtenteId(Long utenteId) { this.utenteId = utenteId; }

    public LocalDate getDataPrenotazione() { return dataPrenotazione; }
    public void setDataPrenotazione(LocalDate dataPrenotazione) { this.dataPrenotazione = dataPrenotazione; }

    public LocalDate getDataEvasione() { return dataEvasione; }
    public void setDataEvasione(LocalDate dataEvasione) { this.dataEvasione = dataEvasione; }

    public String getLibroTitoloSnapshot() { return libroTitoloSnapshot; }
    public void setLibroTitoloSnapshot(String libroTitoloSnapshot) { this.libroTitoloSnapshot = libroTitoloSnapshot; }

    public String getUtenteSnapshot() { return utenteSnapshot; }
    public void setUtenteSnapshot(String utenteSnapshot) { this.utenteSnapshot = utenteSnapshot; }
}
