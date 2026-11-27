package com.employee.employeeservice.controller;

import com.employee.employeeservice.WithAdminUser;
import com.employee.employeeservice.application.service.DeleteEmployeeService;
import com.employee.employeeservice.application.service.UpdateEmployeeService;
import com.employee.employeeservice.domain.model.Employee;
import com.employee.employeeservice.dto.EmployeeResponse;
import com.employee.employeeservice.infrastructure.idempotency.IdempotentEmployeeCreator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
@WithAdminUser
class EmployeeInputWebTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;
    @MockBean private IdempotentEmployeeCreator creator;
    @MockBean private DeleteEmployeeService deleteService;
    @MockBean private UpdateEmployeeService updateService;

    @BeforeEach
    void configureServices() {
        Employee employee = new Employee(7L, "Ana", null, "Garcia", null, "F",
                LocalDate.of(2024, 2, 29), "Developer", LocalDateTime.of(2025, 2, 1, 10, 0), true);
        when(creator.create(isNull(), any())).thenReturn(
                new IdempotentEmployeeCreator.Outcome(EmployeeResponse.from(employee), false));
        when(updateService.execute(eq(7L), any())).thenReturn(employee);
    }

    @Test
    void shouldRejectImpossibleDatesAndContinueTheBatchWithAValidLeapDay() throws Exception {
        postBatch(employee().put("birthDate", "31-02-2023"), employee().put("birthDate", "01-01-0000"),
                employee().put("birthDate", "29-02-2024"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("INVALID"))
                .andExpect(jsonPath("$[1].status").value("INVALID"))
                .andExpect(jsonPath("$[2].status").value("CREATED"));
        verify(creator).create(isNull(), any());

        update(json.createObjectNode().put("birthDate", "29-02-2023"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("MALFORMED_REQUEST"));
        verifyNoInteractions(updateService);
        update(json.createObjectNode().put("birthDate", "29-02-2024")).andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {"firstName", "middleName", "paternalLastName", "maternalLastName", "sex", "jobTitle"})
    void shouldEnforceTheDatabaseCharacterLimitOnCreateAndUpdate(String field) throws Exception {
        // Supplementary Unicode characters occupy two UTF-16 units, but one MySQL character.
        String atLimit = "𠮷".repeat(255);
        String overLimit = atLimit + "a";

        postBatch(employee().put(field, overLimit), employee().put(field, atLimit))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("INVALID"))
                .andExpect(jsonPath("$[0].error", containsString(field)))
                .andExpect(jsonPath("$[1].status").value("CREATED"));
        verify(creator).create(isNull(), any());

        update(json.createObjectNode().put(field, overLimit))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message", containsString(field)));
        verifyNoInteractions(updateService);
        update(json.createObjectNode().put(field, atLimit)).andExpect(status().isOk());
    }

    private ObjectNode employee() {
        return json.createObjectNode().put("firstName", "Ana").put("paternalLastName", "Garcia")
                .put("sex", "F").put("birthDate", "14-03-1995").put("jobTitle", "Developer").put("active", true);
    }

    private ResultActions postBatch(ObjectNode... employees) throws Exception {
        ObjectNode batch = json.createObjectNode();
        var items = batch.putArray("employees");
        for (ObjectNode item : employees) {
            items.add(item);
        }
        return mvc.perform(post("/employees").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsBytes(batch)));
    }

    private ResultActions update(ObjectNode fields) throws Exception {
        return mvc.perform(put("/employees/7").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsBytes(fields)));
    }
}
