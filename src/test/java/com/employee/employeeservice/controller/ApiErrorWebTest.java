package com.employee.employeeservice.controller;

import com.employee.employeeservice.WithAdminUser;
import com.employee.employeeservice.application.service.DeleteEmployeeService;
import com.employee.employeeservice.application.service.GetEmployeeService;
import com.employee.employeeservice.application.service.ListEmployeesService;
import com.employee.employeeservice.application.service.SearchEmployeesService;
import com.employee.employeeservice.application.service.UpdateEmployeeService;
import com.employee.employeeservice.infrastructure.idempotency.IdempotentEmployeeCreator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({EmployeeController.class, EmployeeQueryController.class})
@WithAdminUser
class ApiErrorWebTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private IdempotentEmployeeCreator creator;

    @MockBean
    private DeleteEmployeeService deleteEmployeeService;

    @MockBean
    private GetEmployeeService getEmployeeService;

    @MockBean
    private ListEmployeesService listEmployeesService;

    @MockBean
    private SearchEmployeesService searchEmployeesService;

    @MockBean
    private UpdateEmployeeService updateEmployeeService;

    @Test
    void shouldReportAnInvalidPathParameterWithoutEchoingIt() throws Exception {
        mvc.perform(get("/employees/{id}", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_PARAMETER"))
                .andExpect(jsonPath("$.message").value("Parameter 'id' has an invalid value"));

        verifyNoInteractions(getEmployeeService);
    }

    @Test
    void shouldReportBeanValidationErrorsByField() throws Exception {
        mvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employees\": []}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").value("employees: must not be empty"));

        verifyNoInteractions(creator);
    }

    @Test
    void shouldReportAMalformedBody() throws Exception {
        mvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("MALFORMED_REQUEST"));

        verifyNoInteractions(creator);
    }
}
