package com.employee.employeeservice.controller;

import com.employee.employeeservice.application.service.DeleteEmployeeService;
import com.employee.employeeservice.application.service.GetEmployeeService;
import com.employee.employeeservice.application.service.ListEmployeesService;
import com.employee.employeeservice.application.service.SearchEmployeesService;
import com.employee.employeeservice.application.service.UpdateEmployeeService;
import com.employee.employeeservice.config.SecurityConfiguration;
import com.employee.employeeservice.infrastructure.idempotency.IdempotentEmployeeCreator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({EmployeeQueryController.class, EmployeeController.class})
@Import(SecurityConfiguration.class)
class SecurityWebTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private GetEmployeeService getEmployeeService;

    @MockBean
    private ListEmployeesService listEmployeesService;

    @MockBean
    private SearchEmployeesService searchEmployeesService;

    @MockBean
    private IdempotentEmployeeCreator creator;

    @MockBean
    private DeleteEmployeeService deleteEmployeeService;

    @MockBean
    private UpdateEmployeeService updateEmployeeService;

    @Test
    void shouldRejectAnonymousRequestsWithAJsonError() throws Exception {
        mvc.perform(get("/employees"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", startsWith("Basic")))
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));

        verifyNoInteractions(listEmployeesService);
    }

    @Test
    void shouldAddSecurityHeaders() throws Exception {
        mvc.perform(get("/employees"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"));
    }

    @Test
    @WithMockUser(roles = SecurityConfiguration.READER)
    void shouldLetAReaderRead() throws Exception {
        when(listEmployeesService.execute()).thenReturn(List.of());

        mvc.perform(get("/employees")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = SecurityConfiguration.READER)
    void shouldForbidAReaderFromWriting() throws Exception {
        mvc.perform(delete("/employees/{id}", 7))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));

        mvc.perform(put("/employees/{id}", 7)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jobTitle\": \"Architect\"}"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(deleteEmployeeService, updateEmployeeService);
    }

    @Test
    @WithMockUser(roles = SecurityConfiguration.ADMIN)
    void shouldLetAnAdminWrite() throws Exception {
        mvc.perform(delete("/employees/{id}", 7)).andExpect(status().isNoContent());

        verify(deleteEmployeeService).execute(7L);
    }
}
