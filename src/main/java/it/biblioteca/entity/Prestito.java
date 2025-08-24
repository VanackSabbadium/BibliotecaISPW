// src/main/java/it/biblioteca/entity/Prestito.java
package it.biblioteca.entity;

import java.time.LocalDate;

public class Prestito {
    private Long id;
    private Long libroId; // può essere null se libro cancellato
    private Long utenteId; // può essere null se utente cancellato
    private String utente; // snapshot nome completo (compatibilità)
    private LocalDate dataPrestito;
    private LocalDate dataRestituzione;

    // Snapshot libro
    private String libroIsbnSnapshot;
    private String libroTitoloSnapshot;
    private String libroAutoreSnapshot;

    // Snapshot utente
    private String utenteNomeSnapshot;
    private String utenteCognomeSnapshot;

    public Prestito() {}

    public Prestito(Long id, Long libroId, Long utenteId, String utente,
                    LocalDate dataPrestito, LocalDate dataRestituzione,
                    String libroIsbnSnapshot, String libroTitoloSnapshot, String libroAutoreSnapshot,
                    String utenteNomeSnapshot, String utenteCognomeSnapshot) {
        this.id = id;
        this.libroId = libroId;
        this.utenteId = utenteId;
        this.utente = utente;
        this.dataPrestito = dataPrestito;
        this.dataRestituzione = dataRestituzione;
        this.libroIsbnSnapshot = libroIsbnSnapshot;
        this.libroTitoloSnapshot = libroTitoloSnapshot;
        this.libroAutoreSnapshot = libroAutoreSnapshot;
        this.utenteNomeSnapshot = utenteNomeSnapshot;
        this.utenteCognomeSnapshot = utenteCognomeSnapshot;
    }

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

    public String getLibroIsbnSnapshot() { return libroIsbnSnapshot; }
    public void setLibroIsbnSnapshot(String libroIsbnSnapshot) { this.libroIsbnSnapshot = libroIsbnSnapshot; }

    public String getLibroTitoloSnapshot() { return libroTitoloSnapshot; }
    public void setLibroTitoloSnapshot(String libroTitoloSnapshot) { this.libroTitoloSnapshot = libroTitoloSnapshot; }

    public String getLibroAutoreSnapshot() { return libroAutoreSnapshot; }
    public void setLibroAutoreSnapshot(String libroAutoreSnapshot) { this.libroAutoreSnapshot = libroAutoreSnapshot; }

    public String getUtenteNomeSnapshot() { return utenteNomeSnapshot; }
    public void setUtenteNomeSnapshot(String utenteNomeSnapshot) { this.utenteNomeSnapshot = utenteNomeSnapshot; }

    public String getUtenteCognomeSnapshot() { return utenteCognomeSnapshot; }
    public void setUtenteCognomeSnapshot(String utenteCognomeSnapshot) { this.utenteCognomeSnapshot = utenteCognomeSnapshot; }
}