package com.employee.employeeservice.controller;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class BatchWriteDeadlineTest {

    @Test
    void anExpiredSubmissionCannotStartLater() {
        AtomicReference<Runnable> queued = new AtomicReference<>();
        AtomicBoolean written = new AtomicBoolean();
        BatchWriteDeadline deadline = new BatchWriteDeadline(queued::set, Duration.ofMillis(100));
        assertThatExceptionOfType(BatchWriteDeadline.Unavailable.class)
                .isThrownBy(() -> deadline.execute(() -> written.compareAndSet(false, true)))
                .satisfies(ex -> assertThat(ex.started).isFalse());
        queued.get().run();
        assertThat(written.get()).isFalse();
    }

    @Test
    void aSaturatedExecutorReportsThatTheWriteNeverStarted() {
        BatchWriteDeadline deadline = new BatchWriteDeadline(task -> {
            throw new RejectedExecutionException("Busy");
        }, Duration.ofSeconds(1));
        assertThatExceptionOfType(BatchWriteDeadline.Unavailable.class)
                .isThrownBy(() -> deadline.execute(() -> { throw new AssertionError("Unexpected write"); }))
                .satisfies(ex -> assertThat(ex.started).isFalse());
    }
}
