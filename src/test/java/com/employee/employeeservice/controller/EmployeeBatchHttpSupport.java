package com.employee.employeeservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
abstract class EmployeeBatchHttpSupport {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;

    final String job = "Test-" + UUID.randomUUID();

    ObjectNode employee(String name) {
        return json.createObjectNode()
                .put("firstName", name).put("paternalLastName", "Garcia")
                .put("jobTitle", job).put("sex", "F")
                .put("birthDate", "14-03-1995").put("active", true);
    }

    JsonNode submit(int expectedStatus, ObjectNode... items) throws Exception {
        ObjectNode batch = json.createObjectNode();
        var array = batch.putArray("employees");
        for (ObjectNode item : items) {
            array.add(item);
        }
        String result = mvc.perform(post("/employees").contentType("application/json")
                        .content(json.writeValueAsBytes(batch)))
                .andExpect(status().is(expectedStatus)).andReturn().getResponse().getContentAsString();
        return json.readTree(result);
    }

    int employeeCount() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM employees WHERE job_title = ?", Integer.class, job);
    }

    @AfterEach
    void removeTestEmployees() {
        jdbc.update("DELETE FROM employees WHERE job_title = ?", job);
    }
}
