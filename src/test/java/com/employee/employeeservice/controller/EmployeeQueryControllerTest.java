package com.employee.employeeservice.controller;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EmployeeQueryControllerTest extends EmployeeBatchHttpSupport {

    @Test
    void shouldFindAnEmployeeByTheIdReturnedFromCreation() throws Exception {
        long id = submit(201, employee("Ana")).get(0).get("employee").get("id").asLong();

        mvc.perform(get("/employees/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(equalTo(id), Long.class))
                .andExpect(jsonPath("$.firstName").value("Ana"))
                .andExpect(jsonPath("$.jobTitle").value(job))
                .andExpect(jsonPath("$.birthDate").value("14-03-1995"));
    }

    @Test
    void shouldReturnNotFoundForAnIdThatNoLongerExists() throws Exception {
        long id = submit(201, employee("Eva")).get(0).get("employee").get("id").asLong();
        jdbc.update("DELETE FROM employees WHERE id = ? AND job_title = ?", id, job);

        mvc.perform(get("/employees/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("EMPLOYEE_NOT_FOUND"));
    }
}
