package it.biblioteca.events.events;

import it.biblioteca.events.AppEvent;

public final class PrestitoChanged implements AppEvent {
    public enum Action { REGISTERED, RETURNED }
    public final Action action;
    public final Long prestitoId;
    public PrestitoChanged(Action action, Long prestitoId) { this.action = action; this.prestitoId = prestitoId; }
}