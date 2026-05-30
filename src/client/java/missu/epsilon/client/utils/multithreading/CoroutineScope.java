package missu.epsilon.client.utils.multithreading;

import lombok.Getter;

import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;


//A very simple coroutine model
@Getter
public class CoroutineScope implements AutoCloseable {

    private final Set<CompletableFuture<?>> children = ConcurrentHashMap.newKeySet();
    private final AtomicReference<Throwable> firstFailure = new AtomicReference<>();
    private volatile boolean cancelled = false;

    private static final long CLOSE_TIMEOUT_SECONDS = 10;

    public CompletableFuture<Void> launch(Runnable block) {
        checkIsCancelled();
        CompletableFuture<Void> future = AsyncContext.launch(() -> {
            checkIsCancelled();
            try {
                block.run();
            } catch (Throwable t) {
                handleChildFailure(t);
                throw t;
            }
        });
        track(future);
        return future;
    }

    public <T> Deferred<T> async(Supplier<T> supplier) {
        checkIsCancelled();
        Deferred<T> deferred = AsyncContext.async(() -> {
            checkIsCancelled();
            try {
                return supplier.get();
            } catch (Throwable t) {
                handleChildFailure(t);
                throw t;
            }
        });
        track(deferred.toFuture());
        return deferred;
    }

    public void checkIsCancelled() {
        if (cancelled) throw new CancellationException("Scope was cancelled");
    }

    public void delay(long ms) {
        if (ms <= 0) return;

        long deadline = System.nanoTime() + ms * 1_000_000L;
        long remaining;

        if (ms <= 50) {
            sleepInterruptible(ms);
            checkIsCancelled();
            return;
        }

        while ((remaining = (deadline - System.nanoTime()) / 1_000_000L) > 0) {
            checkIsCancelled();
            sleepInterruptible(Math.min(remaining, 50));
        }
    }

    private static void sleepInterruptible(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CancellationException("Sleep interrupted");
        }
    }

    private void track(CompletableFuture<?> future) {
        children.add(future);
        future.whenComplete((r, e) -> children.remove(future));
    }

    private void handleChildFailure(Throwable t) {
        if (t instanceof CancellationException) return;
        if (firstFailure.compareAndSet(null, t)) {
            System.err.println("[CoroutineScope] First failure detected, cancelling all tasks:");
            t.printStackTrace(); // ← 添加这行
            cancel();
        }
    }

    public void cancel() {
        cancelled = true;
        for (CompletableFuture<?> f : children) {
            f.cancel(true);
        }
    }

    public void joinAll() {
        while (!children.isEmpty()) {
            CompletableFuture<?>[] snapshot = children.toArray(new CompletableFuture[0]);
            try {
                CompletableFuture.allOf(snapshot).join();
            } catch (CompletionException | CancellationException e) {
                e.printStackTrace();
            }
        }

        Throwable failure = firstFailure.get();
        if (failure != null) {
            if (failure instanceof RuntimeException re) throw re;
            if (failure instanceof Error err) throw err;
            throw new CompletionException(failure);
        }
    }

    @Override
    public void close() {
        cancel();
        if (!children.isEmpty()) {
            try {
                CompletableFuture.allOf(children.toArray(new CompletableFuture[0]))
                        .get(CLOSE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                System.err.println("[CoroutineScope] " + children.size()
                        + " task(s) did not terminate within " + CLOSE_TIMEOUT_SECONDS + "s");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException | CancellationException ignored) {
            }
        }
    }

    public static void scoped(Consumer<CoroutineScope> block) {
        try (var scope = new CoroutineScope()) {
            block.accept(scope);
            scope.joinAll();
        }
    }
}
