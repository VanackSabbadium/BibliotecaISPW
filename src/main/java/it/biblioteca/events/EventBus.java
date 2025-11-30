package it.biblioteca.events;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class EventBus {

    // =========================
    //  Singleton Bill Pugh
    // =========================
    private EventBus() {
        // costruttore privato per impedire istanziazione esterna
    }

    // Holder statico che inizializza l'istanza in modo lazy e thread-safe
    private static class Holder {
        private static final EventBus INSTANCE = new EventBus();
    }

    public static EventBus getDefault() {
        return Holder.INSTANCE;
    }

    // =========================
    //  Implementazione EventBus
    // =========================

    private static final class Subscriber<T extends AppEvent> {
        final Class<T> type;
        final Consumer<T> consumer;

        Subscriber(Class<T> type, Consumer<T> consumer) {
            this.type = type;
            this.consumer = consumer;
        }
    }

    private final CopyOnWriteArrayList<Subscriber<?>> subscribers = new CopyOnWriteArrayList<>();

    public <T extends AppEvent> Subscription subscribe(Class<T> type, Consumer<T> consumer) {
        Subscriber<T> s = new Subscriber<>(type, consumer);
        subscribers.add(s);
        return () -> subscribers.remove(s);
    }

    public void publish(AppEvent event) {
        for (Subscriber<?> s : subscribers) {
            if (s.type.isInstance(event)) {
                notifySubscriber(s, event);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends AppEvent> void notifySubscriber(Subscriber<?> raw, AppEvent event) {
        Subscriber<T> s = (Subscriber<T>) raw;
        s.consumer.accept((T) event);
    }
}