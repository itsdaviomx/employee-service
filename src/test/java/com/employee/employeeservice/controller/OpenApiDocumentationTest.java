package com.employee.employeeservice.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasKey;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocumentationTest {

    private static final String ERROR_SCHEMA = "#/components/schemas/ErrorResponse";

    @Autowired
    private MockMvc mvc;

    @Test
    void shouldPublishAllSixEndpoints() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Employee Service API"))
                .andExpect(jsonPath("$.components.securitySchemes.basicAuth.scheme").value("basic"))
                .andExpect(jsonPath("$.paths['/employees']", hasKey("get")))
                .andExpect(jsonPath("$.paths['/employees']", hasKey("post")))
                .andExpect(jsonPath("$.paths['/employees/{id}']", hasKey("get")))
                .andExpect(jsonPath("$.paths['/employees/{id}']", hasKey("put")))
                .andExpect(jsonPath("$.paths['/employees/{id}']", hasKey("delete")))
                .andExpect(jsonPath("$.paths['/employees/search'].get.parameters[0].name").value("name"));
    }

    @Test
    void shouldDocumentStatusCodesAndErrorBodies() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath("$.paths['/employees'].post.responses", hasKey("201")))
                .andExpect(jsonPath("$.paths['/employees'].post.responses", hasKey("413")))
                .andExpect(jsonPath("$.paths['/employees'].post.responses", hasKey("503")))
                .andExpect(jsonPath("$.paths['/employees/{id}'].put.responses", hasKey("409")))
                .andExpect(jsonPath("$.paths['/employees/{id}'].delete.responses", hasKey("204")))
                .andExpect(jsonPath("$.paths['/employees/{id}'].put.responses['404'].content['application/json'].schema.$ref")
                        .value(ERROR_SCHEMA))
                .andExpect(jsonPath("$.paths['/employees/search'].get.responses['400'].content['application/json'].schema.$ref")
                        .value(ERROR_SCHEMA));
    }

    @Test
    void shouldDescribeTheBatchItemsAndTheDateFormat() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath("$.components.schemas.CreateEmployeesRequest.properties.employees.maxItems").value(100))
                .andExpect(jsonPath("$.components.schemas.CreateEmployeesRequest.properties.employees.items.$ref")
                        .value("#/components/schemas/CreateEmployeeRequest"))
                .andExpect(jsonPath("$.components.schemas.CreateEmployeeRequest.properties.birthDate.example")
                        .value("14-03-1995"))
                .andExpect(jsonPath("$.components.schemas.EmployeeResponse.properties.birthDate.example")
                        .value("14-03-1995"));
    }

    @Test
    void shouldServeTheSwaggerUi() throws Exception {
        mvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/swagger-ui/index.html"));

        // Static resource mappings are disabled; the UI must still be served by springdoc.
        mvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }
}
