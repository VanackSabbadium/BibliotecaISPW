package it.biblioteca.bean;

import java.time.LocalDate;

public class PrenotazioneBean {
    private Long id;
    private Long libroId;
    private Long utenteId;
    private LocalDate dataPrenotazione;
    private String libroTitoloSnapshot;
    private String utenteSnapshot;

    public PrenotazioneBean() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getLibroId() { return libroId; }
    public void setLibroId(Long libroId) { this.libroId = libroId; }

    public Long getUtenteId() { return utenteId; }
    public void setUtenteId(Long utenteId) { this.utenteId = utenteId; }

    public LocalDate getDataPrenotazione() { return dataPrenotazione; }
    public void setDataPrenotazione(LocalDate dataPrenotazione) { this.dataPrenotazione = dataPrenotazione; }

    public String getLibroTitoloSnapshot() { return libroTitoloSnapshot; }
    public void setLibroTitoloSnapshot(String libroTitoloSnapshot) { this.libroTitoloSnapshot = libroTitoloSnapshot; }

    public String getUtenteSnapshot() { return utenteSnapshot; }
    public void setUtenteSnapshot(String utenteSnapshot) { this.utenteSnapshot = utenteSnapshot; }
}
