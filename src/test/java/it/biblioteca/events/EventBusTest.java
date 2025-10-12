package it.biblioteca.events;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventBusTest {

    static final class TestEvent implements AppEvent { }

    @Test
    void subscribePublishUnsubscribeWorks() {
        AtomicInteger count = new AtomicInteger();
        EventBus bus = EventBus.getDefault();
        Subscription sub = bus.subscribe(TestEvent.class, e -> count.incrementAndGet());

        bus.publish(new TestEvent());
        assertEquals(1, count.get());

        // unsubscribe invece di close()
        sub.unsubscribe();

        bus.publish(new TestEvent());
        assertEquals(1, count.get());
    }

    @Test
    void multipleSubscribersReceiveEvents() {
        AtomicInteger a = new AtomicInteger();
        AtomicInteger b = new AtomicInteger();
        EventBus bus = EventBus.getDefault();

        Subscription sa = bus.subscribe(TestEvent.class, e -> a.incrementAndGet());
        Subscription sb = bus.subscribe(TestEvent.class, e -> b.incrementAndGet());

        bus.publish(new TestEvent());
        assertEquals(1, a.get());
        assertEquals(1, b.get());

        sa.unsubscribe();

        bus.publish(new TestEvent());
        assertEquals(1, a.get());
        assertEquals(2, b.get());

        sb.unsubscribe();
    }
}