package com.employee.employeeservice.controller;

import org.slf4j.MDC;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/** Limits how long one HTTP batch can wait for all of its independent writes. */
final class BatchWriteDeadline {

    static final class Unavailable extends RuntimeException {
        final boolean started;

        Unavailable(boolean started) {
            super(null, null, false, false);
            this.started = started;
        }
    }

    private final Executor executor;
    private final long deadline;
    private final Map<String, String> requestContext = MDC.getCopyOfContextMap();

    BatchWriteDeadline(Executor executor, Duration budget) {
        this.executor = executor;
        this.deadline = System.nanoTime() + budget.toNanos();
    }

    <T> T execute(Supplier<T> write) {
        if (remainingNanos() <= 0 || Thread.currentThread().isInterrupted()) {
            throw new Unavailable(false);
        }

        // Either the worker claims the write or the request closes it before it starts.
        AtomicBoolean unclaimed = new AtomicBoolean(true);
        FutureTask<T> task = new FutureTask<>(() -> {
            if (remainingNanos() <= 0 || !unclaimed.compareAndSet(true, false)) {
                throw new Unavailable(false);
            }
            Map<String, String> previous = MDC.getCopyOfContextMap();
            try {
                setContext(requestContext);
                return write.get();
            } finally {
                setContext(previous);
            }
        });

        try {
            executor.execute(task);
            return task.get(Math.max(0, remainingNanos()), TimeUnit.NANOSECONDS);
        } catch (RejectedExecutionException ex) {
            throw new Unavailable(false);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (TimeoutException ex) {
            // An in-flight transaction can still commit; report its outcome as unconfirmed.
        } catch (ExecutionException ex) {
            if (ex.getCause() instanceof RuntimeException cause) {
                throw cause;
            }
            if (ex.getCause() instanceof Error cause) {
                throw cause;
            }
            throw new IllegalStateException(ex.getCause());
        }
        boolean started = !unclaimed.getAndSet(false);
        task.cancel(false);
        throw new Unavailable(started);
    }

    private long remainingNanos() {
        return deadline - System.nanoTime();
    }

    private static void setContext(Map<String, String> context) {
        if (context == null) {
            MDC.clear();
        } else {
            MDC.setContextMap(context);
        }
    }
}
