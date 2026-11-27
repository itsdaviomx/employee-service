package com.employee.employeeservice.infrastructure.idempotency;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(name = "app.idempotency.cleanup-enabled", matchIfMissing = true)
@RequiredArgsConstructor
public class ReceiptRetention {

    static final String EXPIRED = "created_at <= CURRENT_TIMESTAMP(6) - INTERVAL 24 HOUR";
    private static final int PAGE_SIZE = 500;

    private final JdbcTemplate jdbc;

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.HOURS)
    public void purgeExpiredReceipts() {
        // Each page commits separately so normal retries can take their row lock between pages.
        int removed;
        do {
            removed = jdbc.update("DELETE FROM employee_creation_receipts WHERE " + EXPIRED
                    + " ORDER BY created_at, request_id LIMIT ?", PAGE_SIZE);
        } while (removed == PAGE_SIZE);
    }
}
