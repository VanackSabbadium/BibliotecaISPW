package it.biblioteca.controller;

import it.biblioteca.bean.PrestitoBean;
import it.biblioteca.entity.Prestito;
import it.biblioteca.service.PrestitoService;

import java.time.LocalDate;
import java.util.List;

public class PrestitoController {
    public enum Esito { OK, UTENTE_INATTIVO, ERRORE_INSERIMENTO }

    private final PrestitoService service;

    public PrestitoController(PrestitoService service) {
        this.service = service;
    }

    public List<Prestito> trovaTutti() { return service.findAll(); }
    public List<Prestito> trovaPrestitiAttivi() { return service.findActive(); }
    public Esito registraPrestito(PrestitoBean bean) { return service.registerLoan(bean); }
    public boolean registraRestituzione(Long prestitoId, LocalDate data) { return service.registerReturn(prestitoId, data); }
}