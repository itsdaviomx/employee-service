package com.employee.employeeservice.infrastructure.idempotency;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReceiptRetentionTest {

    @Test
    void removesExpiredReceiptsInPagesUntilCaughtUp() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        String sql = "DELETE FROM employee_creation_receipts WHERE created_at <= CURRENT_TIMESTAMP(6) - INTERVAL 24 HOUR"
                + " ORDER BY created_at, request_id LIMIT ?";
        when(jdbc.update(sql, 500)).thenReturn(500, 3);

        new ReceiptRetention(jdbc).purgeExpiredReceipts();

        verify(jdbc, times(2)).update(sql, 500);
    }
}
