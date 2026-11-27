package com.employee.employeeservice.controller;

import com.employee.employeeservice.application.exception.EmployeeNotFoundException;
import com.employee.employeeservice.application.service.DeleteEmployeeService;
import com.employee.employeeservice.infrastructure.idempotency.IdempotentEmployeeCreator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
class EmployeeDeleteWebTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private IdempotentEmployeeCreator creator;

    @MockBean
    private DeleteEmployeeService deleteEmployeeService;

    @Test
    void shouldReturnNoContentAfterDeleting() throws Exception {
        mvc.perform(delete("/employees/{id}", 7))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(deleteEmployeeService).execute(7L);
    }

    @Test
    void shouldReturnNotFoundWhenTheEmployeeDoesNotExist() throws Exception {
        doThrow(new EmployeeNotFoundException(7)).when(deleteEmployeeService).execute(7L);

        mvc.perform(delete("/employees/{id}", 7))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("EMPLOYEE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Employee with ID 7 was not found"));
    }
}
