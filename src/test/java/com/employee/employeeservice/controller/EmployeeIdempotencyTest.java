package com.employee.employeeservice.controller;

import com.employee.employeeservice.config.SecurityConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@Sql("/db/mysql/001_employee_creation_receipts.sql")
class EmployeeIdempotencyTest extends EmployeeBatchHttpSupport {

    private final String keyA = UUID.randomUUID().toString();
    private final String keyB = UUID.randomUUID().toString();
    private final String keyC = UUID.randomUUID().toString();

    @AfterEach
    void removeTestReceipts() {
        jdbc.update("DELETE FROM employee_creation_receipts WHERE request_id IN (?, ?, ?)", keyA, keyB, keyC);
    }

    @Test
    void shouldReplayConfirmedItemsAfterReorderingAndAllowCorrectionOfAnInvalidItem() throws Exception {
        var ana = employee("Ana").put("requestId", keyA);
        var luis = employee("Luis").put("requestId", keyB).put("age", 0);
        var eva = employee("Eva").put("requestId", keyC);

        var first = submit(200, ana, luis, eva);
        var retry = submit(200, eva, luis, ana);

        assertThat(retry.findValuesAsText("status")).containsExactly("REPLAYED", "INVALID", "REPLAYED");
        assertThat(retry.get(0).get("employee")).isEqualTo(first.get(2).get("employee"));
        assertThat(retry.get(2).get("employee")).isEqualTo(first.get(0).get("employee"));
        assertThat(employeeCount()).isEqualTo(2);

        luis.remove("age");
        assertThat(submit(201, luis).get(0).get("status").asText()).isEqualTo("CREATED");
        assertThat(employeeCount()).isEqualTo(3);
    }

    @Test
    void shouldRejectChangedPayloadAndStillProcessTheNextItem() throws Exception {
        var ana = employee("Ana").put("requestId", keyA);
        submit(201, ana);
        var result = submit(200, ana.put("firstName", "Changed"), employee("Eva").put("requestId", keyB));

        assertThat(result.findValuesAsText("status")).containsExactly("CONFLICT", "CREATED");
        assertThat(employeeCount()).isEqualTo(2);
    }

    @Test
    void shouldReplayTheOriginalResponseAfterAnEmployeeIsEdited() throws Exception {
        var ana = employee("Ana").put("requestId", keyA);
        var original = submit(201, ana).get(0).get("employee");
        jdbc.update("UPDATE employees SET first_name = ? WHERE id = ?", "Updated", original.get("id").asLong());

        var replay = submit(200, ana).get(0);
        assertThat(replay.get("status").asText()).isEqualTo("REPLAYED");
        assertThat(replay.get("employee")).isEqualTo(original);
        assertThat(employeeCount()).isEqualTo(1);
    }

    @Test
    void retryAfterDeleteReplaysUntilExpiryThenCreatesANewEmployee() throws Exception {
        var ana = employee("Ana").put("requestId", keyA);
        var original = submit(201, ana).get(0).get("employee");
        long originalId = original.get("id").asLong();
        mvc.perform(delete("/employees/{id}", originalId)).andExpect(status().isNoContent());
        assertThat(submit(200, ana).get(0).get("employee")).isEqualTo(original);
        assertThat(employeeCount()).isZero();

        // Leave the expired row present to exercise retry before the next scheduled purge.
        jdbc.update("UPDATE employee_creation_receipts SET created_at = NOW(6) - INTERVAL 24 HOUR WHERE request_id = ?", keyA);
        var replacement = submit(201, ana).get(0).get("employee");
        assertThat(replacement.get("id").asLong()).isNotEqualTo(originalId);
        assertThat(submit(200, ana).get(0).get("employee")).isEqualTo(replacement);
        assertThat(employeeCount()).isOne();
    }

    @Test
    void simultaneousRequestsShouldCommitOnlyOneEmployee() throws Exception {
        var pool = Executors.newFixedThreadPool(2);
        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);
        Callable<JsonNode> send = () -> {
            ready.countDown();
            if (!start.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out waiting for both requests");
            }
            var batch = json.createObjectNode();
            batch.putArray("employees").add(employee("Ana").put("requestId", keyA));
            var result = mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                            .post("/employees").contentType("application/json").content(json.writeValueAsBytes(batch))
                            .with(user("admin").roles(SecurityConfiguration.ADMIN)))
                    .andReturn().getResponse();
            assertThat(result.getStatus()).isIn(200, 201);
            return json.readTree(result.getContentAsString()).get(0);
        };
        try {
            var one = pool.submit(send);
            var two = pool.submit(send);
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            var a = one.get(15, TimeUnit.SECONDS);
            var b = two.get(15, TimeUnit.SECONDS);
            assertThat(List.of(a.get("status").asText(), b.get("status").asText()))
                    .containsExactlyInAnyOrder("CREATED", "REPLAYED");
            assertThat(a.get("employee")).isEqualTo(b.get("employee"));
            assertThat(employeeCount()).isEqualTo(1);
        } finally {
            pool.shutdownNow();
        }
    }
}
