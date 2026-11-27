package com.employee.employeeservice.controller;

import com.employee.employeeservice.WithAdminUser;
import com.employee.employeeservice.application.exception.EmployeeNotFoundException;
import com.employee.employeeservice.application.port.in.UpdateEmployeeCommand;
import com.employee.employeeservice.application.service.DeleteEmployeeService;
import com.employee.employeeservice.application.service.UpdateEmployeeService;
import com.employee.employeeservice.domain.exception.InvalidEmployeeException;
import com.employee.employeeservice.domain.model.Employee;
import com.employee.employeeservice.infrastructure.idempotency.IdempotentEmployeeCreator;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
@WithAdminUser
class EmployeeUpdateWebTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private IdempotentEmployeeCreator creator;

    @MockBean
    private DeleteEmployeeService deleteEmployeeService;

    @MockBean
    private UpdateEmployeeService updateEmployeeService;

    @Test
    void shouldSendOnlyTheProvidedFieldsAndReturnTheUpdatedEmployee() throws Exception {
        Employee updated = new Employee(
                7L, "Ana", null, "Garcia", null, "F",
                LocalDate.of(1995, 3, 14), "Architect",
                LocalDateTime.of(2026, 1, 15, 10, 30), false
        );
        when(updateEmployeeService.execute(eq(7L), any())).thenReturn(updated);

        update(7, "{\"jobTitle\": \"Architect\", \"active\": false}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.jobTitle").value("Architect"))
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.birthDate").value("14-03-1995"));

        ArgumentCaptor<UpdateEmployeeCommand> captor = ArgumentCaptor.forClass(UpdateEmployeeCommand.class);
        verify(updateEmployeeService).execute(eq(7L), captor.capture());
        assertThat(captor.getValue()).isEqualTo(new UpdateEmployeeCommand(
                null, null, null, null, null, null, null, "Architect", false
        ));
    }

    @Test
    void shouldRejectABlankFieldWithoutCallingTheService() throws Exception {
        update(7, "{\"firstName\": \"   \"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").value("firstName: must not be blank"));

        verifyNoInteractions(updateEmployeeService);
    }

    @Test
    void shouldRejectABirthDateWithTheWrongFormat() throws Exception {
        update(7, "{\"birthDate\": \"1995-03-14\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("MALFORMED_REQUEST"));

        verifyNoInteractions(updateEmployeeService);
    }

    @Test
    void shouldReturnNotFoundWhenTheEmployeeDoesNotExist() throws Exception {
        when(updateEmployeeService.execute(eq(7L), any())).thenThrow(new EmployeeNotFoundException(7));

        update(7, "{\"jobTitle\": \"Architect\"}")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("EMPLOYEE_NOT_FOUND"));
    }

    @Test
    void shouldReturnBadRequestWhenTheDomainRejectsTheChange() throws Exception {
        when(updateEmployeeService.execute(eq(7L), any()))
                .thenThrow(new InvalidEmployeeException("Age must match the birth date"));

        update(7, "{\"age\": 1}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_EMPLOYEE"))
                .andExpect(jsonPath("$.message").value("Age must match the birth date"));
    }

    private ResultActions update(long id, String body) throws Exception {
        return mvc.perform(put("/employees/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }
}
