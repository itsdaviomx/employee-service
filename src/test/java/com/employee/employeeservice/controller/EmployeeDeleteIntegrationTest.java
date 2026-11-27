package com.employee.employeeservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EmployeeDeleteIntegrationTest extends EmployeeBatchHttpSupport {

    @Test
    void shouldDeleteOnlyTheRequestedEmployee() throws Exception {
        JsonNode created = submit(201, employee("Ana"), employee("Eva"));
        long anaId = idAt(created, 0);
        long evaId = idAt(created, 1);

        mvc.perform(delete("/employees/{id}", anaId))
                .andExpect(status().isNoContent());

        mvc.perform(get("/employees/{id}", anaId))
                .andExpect(status().isNotFound());
        mvc.perform(get("/employees/{id}", evaId))
                .andExpect(status().isOk());
        assertThat(employeeCount()).isEqualTo(1);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingTheSameEmployeeTwice() throws Exception {
        long id = idAt(submit(201, employee("Ana")), 0);

        mvc.perform(delete("/employees/{id}", id))
                .andExpect(status().isNoContent());

        mvc.perform(delete("/employees/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("EMPLOYEE_NOT_FOUND"));
        assertThat(employeeCount()).isZero();
    }

    private static long idAt(JsonNode created, int index) {
        return created.get(index).path("employee").path("id").asLong();
    }
}
