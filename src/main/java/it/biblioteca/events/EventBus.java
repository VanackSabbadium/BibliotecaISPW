package it.biblioteca.events;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public enum EventBus {
    INSTANCE;

    public static EventBus getDefault() { return INSTANCE; }

    private static final class Subscriber<T extends AppEvent> {
        final Class<T> type;
        final Consumer<T> consumer;
        Subscriber(Class<T> type, Consumer<T> consumer) { this.type = type; this.consumer = consumer; }
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