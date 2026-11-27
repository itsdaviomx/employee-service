package com.employee.employeeservice.controller;

import com.employee.employeeservice.WithAdminUser;
import com.employee.employeeservice.application.exception.InvalidSearchTermException;
import com.employee.employeeservice.application.service.GetEmployeeService;
import com.employee.employeeservice.application.service.ListEmployeesService;
import com.employee.employeeservice.application.service.SearchEmployeesService;
import com.employee.employeeservice.domain.model.Employee;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeQueryController.class)
@WithAdminUser
class EmployeeSearchWebTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private GetEmployeeService getEmployeeService;

    @MockBean
    private ListEmployeesService listEmployeesService;

    @MockBean
    private SearchEmployeesService searchEmployeesService;

    @Test
    void shouldReturnTheMatchingEmployees() throws Exception {
        Employee ana = new Employee(
                7L, "Ana", "Maria", "Garcia", "Lopez", "F",
                LocalDate.of(1995, 3, 14), "Developer",
                LocalDateTime.of(2026, 1, 15, 10, 30), true
        );
        when(searchEmployeesService.execute("ana")).thenReturn(List.of(ana));

        mvc.perform(get("/employees/search").param("name", "ana"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].firstName").value("Ana"))
                .andExpect(jsonPath("$[0].birthDate").value("14-03-1995"));
    }

    @Test
    void shouldReturnAnEmptyArrayWhenNothingMatches() throws Exception {
        when(searchEmployeesService.execute("zz")).thenReturn(List.of());

        mvc.perform(get("/employees/search").param("name", "zz"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]", true));
    }

    @Test
    void shouldReturnBadRequestWhenTheNameParameterIsMissing() throws Exception {
        mvc.perform(get("/employees/search"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("MISSING_PARAMETER"));

        verifyNoInteractions(searchEmployeesService);
    }

    @Test
    void shouldReturnBadRequestWhenTheSearchTermIsRejected() throws Exception {
        when(searchEmployeesService.execute(" "))
                .thenThrow(new InvalidSearchTermException("Search name must not be blank"));

        mvc.perform(get("/employees/search").param("name", " "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_SEARCH_TERM"))
                .andExpect(jsonPath("$.message").value("Search name must not be blank"));
    }
}
