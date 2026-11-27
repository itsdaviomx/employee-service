package com.employee.employeeservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EmployeeSearchIntegrationTest extends EmployeeBatchHttpSupport {

    @Test
    void shouldMatchAnyNameFieldIgnoringCaseAndIncludeInactiveEmployees() throws Exception {
        JsonNode created = submit(201,
                employee("Mariana"),
                employee("Eva").put("middleName", "Maria").put("active", false),
                employee("Luis").put("maternalLastName", "Ramirez"),
                employee("Pedro"));

        assertThat(searchIds("MARI")).containsExactly(idAt(created, 0), idAt(created, 1));
        assertThat(searchIds("rez")).containsExactly(idAt(created, 2));
        assertThat(searchIds("garc")).hasSize(4);
    }

    @Test
    void shouldTreatLikeWildcardsAsLiteralCharacters() throws Exception {
        JsonNode created = submit(201, employee("Ana"), employee("Ana_Luisa"));

        assertThat(searchIds("_")).containsExactly(idAt(created, 1));
        assertThat(searchIds("%")).isEmpty();
    }

    private List<Long> searchIds(String name) throws Exception {
        String response = mvc.perform(get("/employees/search").param("name", name))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<Long> ids = new ArrayList<>();
        for (JsonNode employee : json.readTree(response)) {
            if (job.equals(employee.path("jobTitle").asText())) {
                ids.add(employee.path("id").asLong());
            }
        }
        return ids;
    }

    private static long idAt(JsonNode created, int index) {
        return created.get(index).path("employee").path("id").asLong();
    }
}
