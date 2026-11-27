package com.employee.employeeservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
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
    void shouldReturnNotFoundForADeletedEmployee() throws Exception {
        long id = createAna();
        mvc.perform(delete("/employees/{id}", id)).andExpect(status().isNoContent());

        update(id, "{\"firstName\": \"Eva\"}")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("EMPLOYEE_NOT_FOUND"));
        assertThat(employeeCount()).isZero();
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
