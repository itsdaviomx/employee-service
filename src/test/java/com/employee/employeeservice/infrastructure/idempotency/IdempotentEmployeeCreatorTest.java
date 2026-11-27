package com.employee.employeeservice.infrastructure.idempotency;

import com.employee.employeeservice.application.port.in.CreateEmployeeCommand;
import com.employee.employeeservice.application.service.CreateEmployeeService;
import com.employee.employeeservice.domain.model.Employee;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IdempotentEmployeeCreatorTest {

    @Test
    void expiredReceiptWithAnotherPayloadAllowsANewCreationBeforePhysicalPurge() throws Exception {
        UUID key = UUID.randomUUID();
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        ResultSet row = mock(ResultSet.class);
        when(row.getString("payload_hash")).thenReturn("previous-hash");
        when(row.getString("response_json")).thenReturn("{}");
        when(row.getBoolean("expired")).thenReturn(true);
        when(jdbc.queryForObject(contains("FOR UPDATE"), ArgumentMatchers.<RowMapper<Object>>any(), eq(key.toString())))
                .thenAnswer(call -> call.<RowMapper<?>>getArgument(1).mapRow(row, 0));

        CreateEmployeeService creator = mock(CreateEmployeeService.class);
        LocalDate birthDate = LocalDate.of(2000, 1, 1);
        CreateEmployeeCommand command = new CreateEmployeeCommand(
                "Eva", null, "Ruiz", null, "F", birthDate, null, "Engineer", true);
        when(creator.execute(command)).thenReturn(new Employee(
                12L, "Eva", null, "Ruiz", null, "F", birthDate, "Engineer", LocalDateTime.now(), true));
        var service = new IdempotentEmployeeCreator(jdbc, new ObjectMapper().findAndRegisterModules(), creator);

        var result = service.create(key, command);

        assertThat(result.replayed()).isFalse();
        assertThat(result.employee().id()).isEqualTo(12L);
        verify(jdbc).update(contains("created_at = CURRENT_TIMESTAMP(6)"), anyString(),
                contains("\"id\":12"), eq(key.toString()));
    }
}
