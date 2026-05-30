package missu.epsilon.client.utils.multithreading;

import lombok.experimental.UtilityClass;
import missu.epsilon.client.utils.Wrapper;

import java.util.concurrent.*;
import java.util.function.Supplier;

@UtilityClass
public class AsyncContext implements Wrapper {
    private static final ExecutorService EXECUTOR = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("epsilon-async-", 0).factory());

    public ExecutorService executor() {
        return EXECUTOR;
    }

    public CompletableFuture<Void> launch(Runnable block) {
        return CompletableFuture.runAsync(block, EXECUTOR);
    }

    public <T> Deferred<T> async(Supplier<T> supplier) {
        return new Deferred<>(CompletableFuture.supplyAsync(supplier, EXECUTOR));
    }

    public CompletableFuture<Void> awaitAll(CompletableFuture<?>... futures) {
        return CompletableFuture.allOf(futures);
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public <T> T awaitAny(CompletableFuture<T>... futures) {
        return (T) CompletableFuture.anyOf(futures).join();
    }

    public <T> T withMainThreadBlocking(Supplier<T> supplier) {
        if (mc.isOnThread()) {
            return supplier.get();
        }
        try {
            return mc.submit(supplier).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CancellationException("Main thread execution interrupted");
        } catch (ExecutionException e) {
            throw unwrap(e);
        }
    }

    public void runOnMainThreadBlocking(Runnable runnable) {
        if (mc.isOnThread()) {
            runnable.run();
            return;
        }
        try {
            mc.submit(runnable).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CancellationException("Main thread execution interrupted");
        } catch (ExecutionException e) {
            throw unwrap(e);
        }
    }

    public void queueToMainThread(Runnable runnable) {
        mc.execute(runnable);
    }

    public void delay(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CancellationException("Coroutine was cancelled");
        }
    }

    private RuntimeException unwrap(ExecutionException e) {
        Throwable cause = e.getCause();
        if (cause instanceof RuntimeException re) throw re;
        if (cause instanceof Error err) throw err;
        return new CompletionException(cause);
    }
}
