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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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

    @Test
    void shouldReportAnUnsupportedMethodAndListTheAllowedOnes() throws Exception {
        mvc.perform(delete("/employees"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().string("Allow", containsString("GET")))
                .andExpect(jsonPath("$.error").value("METHOD_NOT_ALLOWED"));
    }

    @Test
    void shouldReportAnUnsupportedMediaTypeAndListTheAcceptedOnes() throws Exception {
        mvc.perform(post("/employees")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("Ana"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(header().string("Accept", containsString("application/json")))
                .andExpect(jsonPath("$.error").value("UNSUPPORTED_MEDIA_TYPE"));

        verifyNoInteractions(creator);
    }

    @Test
    void shouldReportAnUnknownRoute() throws Exception {
        mvc.perform(get("/departments"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void shouldHideTheDetailsOfAnUnexpectedError() throws Exception {
        when(getEmployeeService.execute(7L))
                .thenThrow(new IllegalStateException("jdbc:mysql://db:3306 password=secret"));

        mvc.perform(get("/employees/{id}", 7))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("INTERNAL_ERROR"))
                .andExpect(content().string(not(containsString("secret"))))
                .andExpect(content().string(not(containsString("IllegalStateException"))));
    }
}
