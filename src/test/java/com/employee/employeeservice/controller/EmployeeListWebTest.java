package com.employee.employeeservice.controller;

import com.employee.employeeservice.application.service.GetEmployeeService;
import com.employee.employeeservice.application.service.ListEmployeesService;
import com.employee.employeeservice.application.service.SearchEmployeesService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeQueryController.class)
class EmployeeListWebTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private GetEmployeeService getEmployeeService;

    @MockBean
    private ListEmployeesService listEmployeesService;

    @MockBean
    private SearchEmployeesService searchEmployeesService;

    @Test
    void shouldReturnAnEmptyArrayWhenThereAreNoEmployees() throws Exception {
        when(listEmployeesService.execute()).thenReturn(List.of());

        mvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]", true));
    }
}
