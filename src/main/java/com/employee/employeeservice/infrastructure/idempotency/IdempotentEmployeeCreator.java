package com.employee.employeeservice.infrastructure.idempotency;

import com.employee.employeeservice.application.port.in.CreateEmployeeCommand;
import com.employee.employeeservice.application.service.CreateEmployeeService;
import com.employee.employeeservice.dto.EmployeeResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class IdempotentEmployeeCreator {

    public record Outcome(EmployeeResponse employee, boolean replayed) {}
    private record Receipt(String payloadHash, String responseJson) {}

    public static class RequestIdConflict extends RuntimeException {
        public RequestIdConflict() {
            super("This requestId was already used with different employee data");
        }
    }

    private final JdbcTemplate jdbc;
    private final ObjectMapper json;
    private final CreateEmployeeService creator;

    public IdempotentEmployeeCreator(JdbcTemplate jdbc, ObjectMapper json,
                                     CreateEmployeeService creator) {
        this.jdbc = jdbc;
        this.json = json.copy().configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
        this.creator = creator;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Outcome create(UUID requestId, CreateEmployeeCommand command) {
        if (requestId == null) {
            return new Outcome(EmployeeResponse.from(creator.execute(command)), false);
        }

        String key = requestId.toString();
        try {
            String hash = sha256(json.writeValueAsBytes(command));

            // This upsert serializes requests for the same key, including first use.
            jdbc.update("""
                    INSERT INTO employee_creation_receipts (request_id, payload_hash)
                    VALUES (?, ?)
                    ON DUPLICATE KEY UPDATE request_id = request_id
                    """, key, hash);

            Receipt receipt = jdbc.queryForObject("""
                    SELECT payload_hash, response_json
                    FROM employee_creation_receipts WHERE request_id = ? FOR UPDATE
                    """, (row, number) -> new Receipt(row.getString(1), row.getString(2)), key);

            if (!hash.equals(receipt.payloadHash())) {
                throw new RequestIdConflict();
            }
            if (receipt.responseJson() != null) {
                return new Outcome(json.readValue(receipt.responseJson(), EmployeeResponse.class), true);
            }

            EmployeeResponse employee = EmployeeResponse.from(creator.execute(command));
            jdbc.update("""
                    UPDATE employee_creation_receipts SET response_json = ? WHERE request_id = ?
                    """, json.writeValueAsString(employee), key);
            return new Outcome(employee, false);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Cannot encode or read employee creation receipt", exception);
        }
    }

    private static String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
