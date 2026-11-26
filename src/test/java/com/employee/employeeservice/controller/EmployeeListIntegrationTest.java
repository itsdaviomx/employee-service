package com.employee.employeeservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EmployeeListIntegrationTest extends EmployeeBatchHttpSupport {

    @Test
    void shouldListActiveAndInactiveEmployeesInIdOrder() throws Exception {
        JsonNode created = submit(201, employee("Ana"), employee("Eva").put("active", false));

        String response = mvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode employees = json.readTree(response);
        assertThat(employees.isArray()).isTrue();

        List<Long> ids = new ArrayList<>();
        for (JsonNode employee : employees) {
            if (job.equals(employee.path("jobTitle").asText())) {
                ids.add(employee.path("id").asLong());
            }
        }
        assertThat(ids).containsExactly(
                created.get(0).path("employee").path("id").asLong(),
                created.get(1).path("employee").path("id").asLong()
        );
    }
}
