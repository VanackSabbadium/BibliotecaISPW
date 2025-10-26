package it.biblioteca.controller;

import it.biblioteca.bean.UtenteBean;
import it.biblioteca.entity.Utente;
import it.biblioteca.service.UtenteService;

import java.util.List;
import java.util.Optional;

public class UtenteController {
    private final UtenteService service;

    public UtenteController(UtenteService service) {
        this.service = service;
    }

    public List<Utente> trovaTutti() { return service.findAll(); }
    public List<Utente> trovaAttivi() { return service.findActive(); }
    public boolean aggiungi(UtenteBean b) { return service.add(b); }
    public boolean aggiorna(UtenteBean b) { return service.update(b); }
    public boolean elimina(Long id) { return service.delete(id); }
    public boolean creaCredenziali(Long utenteId, String username, String passwordPlain) { return service.createCredentials(utenteId, username, passwordPlain); }
    public boolean aggiornaCredenziali(Long utenteId, String username, String passwordPlain) { return service.updateCredentials(utenteId, username, passwordPlain); }
    public Optional<String> getUsernameForUserId(Long utenteId) { return service.getUsernameForUserId(utenteId); }
}