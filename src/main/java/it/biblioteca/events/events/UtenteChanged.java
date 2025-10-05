package it.biblioteca.events.events;

import it.biblioteca.events.AppEvent;

public final class UtenteChanged implements AppEvent {
    public enum Action { ADDED, UPDATED, DELETED, CREDENTIALS_CHANGED }
    public final Action action;
    public final Long utenteId;
    public UtenteChanged(Action action, Long utenteId) { this.action = action; this.utenteId = utenteId; }
}
