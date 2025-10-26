package it.biblioteca.events.events;

import it.biblioteca.events.AppEvent;

public final class BookChanged implements AppEvent {
    public enum Action { ADDED, UPDATED, REMOVED }
    public final Action action;
    public final Long bookId;
    public BookChanged(Action action, Long bookId) { this.action = action; this.bookId = bookId; }
}