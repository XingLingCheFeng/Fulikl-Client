package missu.epsilon.client.utils.multithreading;

import java.util.concurrent.*;
import java.util.function.Function;

public class Deferred<T> {

    private final CompletableFuture<T> future;

    public Deferred(CompletableFuture<T> future) {
        this.future = future;
    }

    public T await() {
        try {
            return future.get();
        } catch (ExecutionException e) {
            throw unwrap(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CancellationException("Deferred was interrupted");
        } catch (CancellationException e) {
            throw e;
        }
    }

    public T await(long timeout, TimeUnit unit) {
        try {
            return future.get(timeout, unit);
        } catch (TimeoutException e) {
            throw new RuntimeException("Deferred timed out after " + timeout + " " + unit, e);
        } catch (ExecutionException e) {
            throw unwrap(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CancellationException("Deferred was interrupted");
        }
    }

    public <R> Deferred<R> map(Function<T, R> mapper) {
        return new Deferred<>(future.thenApplyAsync(mapper, AsyncContext.executor()));
    }

    public <R> Deferred<R> flatMap(Function<T, Deferred<R>> mapper) {
        CompletableFuture<R> composed = future.thenComposeAsync(
                value -> mapper.apply(value).toFuture(),
                AsyncContext.executor()
        );
        return new Deferred<>(composed);
    }

    public Deferred<T> onComplete(java.util.function.BiConsumer<T, Throwable> action) {
        future.whenCompleteAsync(action, AsyncContext.executor());
        return this;
    }

    public boolean isDone() {
        return future.isDone();
    }

    public boolean isCancelled() {
        return future.isCancelled();
    }

    public boolean cancel() {
        return future.cancel(true);
    }

    public CompletableFuture<T> toFuture() {
        return future;
    }

    public static <T> Deferred<T> completed(T value) {
        return new Deferred<>(CompletableFuture.completedFuture(value));
    }

    public static <T> Deferred<T> failed(Throwable t) {
        return new Deferred<>(CompletableFuture.failedFuture(t));
    }

    private RuntimeException unwrap(ExecutionException e) {
        Throwable cause = e.getCause();
        if (cause instanceof RuntimeException re) return re;
        if (cause instanceof Error err) throw err;
        return new CompletionException(cause);
    }
}
