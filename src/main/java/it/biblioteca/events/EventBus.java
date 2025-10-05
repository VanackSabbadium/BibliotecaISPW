package it.biblioteca.events;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class EventBus {
    private static final EventBus INSTANCE = new EventBus();
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
                @SuppressWarnings("unchecked")
                Consumer<AppEvent> c = (Consumer<AppEvent>) s.consumer;
                c.accept(event);
            }
        }
    }
}