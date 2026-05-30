package missu.epsilon.client.utils.multithreading;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class Channel<T> implements AutoCloseable {

    private static final Object CLOSED_TOKEN = new Object();

    private static final long POLL_INTERVAL_MS = 50;

    private final BlockingQueue<Object> queue;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    public Channel() {
        this.queue = new SynchronousQueue<>();
    }

    public Channel(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("capacity must be > 0");
        this.queue = new LinkedBlockingQueue<>(capacity);
    }

    public void send(T value) {
        if (closed.get()) throw new ChannelClosedException("Channel is closed");
        try {
            while (!closed.get()) {
                if (queue.offer(value, POLL_INTERVAL_MS, TimeUnit.MILLISECONDS)) {
                    return;
                }
            }
            throw new ChannelClosedException("Channel was closed during send");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CancellationException("Send was interrupted");
        }
    }

    @SuppressWarnings("unchecked")
    public T receive() {
        while (true) {
            try {
                Object val = queue.poll(POLL_INTERVAL_MS, TimeUnit.MILLISECONDS);

                if (val == CLOSED_TOKEN) {
                    queue.offer(CLOSED_TOKEN);
                    return null;
                }

                if (val != null) {
                    return (T) val;
                }

                if (closed.get() && queue.isEmpty()) {
                    return null;
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
    }

    public boolean trySend(T value) {
        if (closed.get()) return false;
        return queue.offer(value);
    }

    @SuppressWarnings("unchecked")
    public T tryReceive() {
        Object val = queue.poll();
        if (val == CLOSED_TOKEN) {
            queue.offer(CLOSED_TOKEN);
            return null;
        }
        return val != null ? (T) val : null;
    }

    public void consumeEach(Consumer<T> action) {
        T value;
        while ((value = receive()) != null) {
            action.accept(value);
        }
    }

    public boolean isClosed() {
        return closed.get();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            queue.offer(CLOSED_TOKEN);
        }
    }

    public static class ChannelClosedException extends IllegalStateException {
        public ChannelClosedException(String message) {
            super(message);
        }
    }
}
