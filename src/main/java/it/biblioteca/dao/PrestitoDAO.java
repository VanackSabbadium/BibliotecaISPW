package it.biblioteca.dao;

import it.biblioteca.bean.PrestitoBean;
import it.biblioteca.entity.Prestito;

import java.time.LocalDate;
import java.util.List;

public interface PrestitoDAO {

    List<Prestito> trovaTutti();
    List<Prestito> trovaPrestitiAttivi();
    boolean inserisci(PrestitoBean bean);
    boolean chiudiPrestito(Long prestitoId, LocalDate dataRestituzione);
}