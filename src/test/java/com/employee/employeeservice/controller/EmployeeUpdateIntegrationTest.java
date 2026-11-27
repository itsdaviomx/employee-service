package com.employee.employeeservice.controller;

import com.employee.employeeservice.application.port.in.UpdateEmployeeCommand;
import com.employee.employeeservice.application.service.UpdateEmployeeService;
import com.employee.employeeservice.domain.exception.InvalidEmployeeException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// The tests never change jobTitle: the inherited cleanup deletes rows by that marker.
class EmployeeUpdateIntegrationTest extends EmployeeBatchHttpSupport {

    @Test
    void shouldPersistOnlyTheSentFieldsAndKeepTheRest() throws Exception {
        long id = createAna();
        JsonNode before = fetch(id);

        update(id, "{\"firstName\": \"Ana Sofia\", \"active\": false}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Ana Sofia"));

        JsonNode after = fetch(id);
        assertThat(after.path("firstName").asText()).isEqualTo("Ana Sofia");
        assertThat(after.path("active").asBoolean()).isFalse();
        assertThat(after.path("paternalLastName")).isEqualTo(before.path("paternalLastName"));
        assertThat(after.path("birthDate")).isEqualTo(before.path("birthDate"));
        assertThat(after.path("jobTitle")).isEqualTo(before.path("jobTitle"));
        assertThat(after.path("createdAt")).isEqualTo(before.path("createdAt"));
        assertThat(employeeCount()).isEqualTo(1);
    }

    @Test
    void shouldLeaveTheEmployeeUnchangedWhenTheDomainRejectsTheUpdate() throws Exception {
        long id = createAna();

        update(id, "{\"firstName\": \"Changed\", \"age\": 1}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_EMPLOYEE"));

        assertThat(fetch(id).path("firstName").asText()).isEqualTo("Ana");
    }

    @Test
    void shouldSurfaceTheDomainErrorAlthoughItIsRaisedInsideTheRowLockTransaction() throws Exception {
        long id = createAna();

        // The rule runs inside EmployeePersistenceAdapter (@Repository); it must not become a data access error.
        assertThatThrownBy(() -> updateService.execute(id, new UpdateEmployeeCommand(
                null, null, null, null, null, null, 1, null, null)))
                .isExactlyInstanceOf(InvalidEmployeeException.class);
    }

    @Test
    void shouldReturnNotFoundForADeletedEmployee() throws Exception {
        long id = createAna();
        mvc.perform(delete("/employees/{id}", id)).andExpect(status().isNoContent());

        update(id, "{\"firstName\": \"Eva\"}")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("EMPLOYEE_NOT_FOUND"));
        assertThat(employeeCount()).isZero();
    }

    @Autowired private PlatformTransactionManager transactions;
    @Autowired private UpdateEmployeeService updateService;

    @Test
    void partialUpdateUsesLatestCommittedRowEvenWhenItsTransactionHasAnOlderSnapshot() throws Exception {
        long id = createAna();
        TransactionTemplate first = new TransactionTemplate(transactions);
        first.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
        TransactionTemplate other = new TransactionTemplate(transactions);
        other.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        first.executeWithoutResult(tx -> {
            // Establish the old InnoDB snapshot without caching the employee in Hibernate.
            assertThat(jdbc.queryForObject("SELECT first_name FROM employees WHERE id = ?", String.class, id))
                    .isEqualTo("Ana");
            other.executeWithoutResult(inner -> updateService.execute(id, new UpdateEmployeeCommand(
                    "Eva", null, null, null, null, null, null, null, null)));
            updateService.execute(id, new UpdateEmployeeCommand(
                    null, null, null, null, null, null, null, null, false));
        });
        JsonNode after = fetch(id);
        assertThat(after.path("firstName").asText()).isEqualTo("Eva");
        assertThat(after.path("active").asBoolean()).isFalse();
    }

    private long createAna() throws Exception {
        return submit(201, employee("Ana")).get(0).path("employee").path("id").asLong();
    }

    private JsonNode fetch(long id) throws Exception {
        String body = mvc.perform(get("/employees/{id}", id))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(body);
    }

    private ResultActions update(long id, String body) throws Exception {
        return mvc.perform(put("/employees/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }
}
