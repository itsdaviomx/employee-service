package com.employee.employeeservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Authenticates with the real users configured from the environment, through HTTP Basic.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ApiUsersIntegrationTest {

    private static final String EMPTY_BATCH = "{\"employees\": []}";

    @Autowired
    private MockMvc mvc;

    @Value("${app.security.admin.username}")
    private String adminUsername;

    @Value("${app.security.admin.password}")
    private String adminPassword;

    @Value("${app.security.reader.username}")
    private String readerUsername;

    @Value("${app.security.reader.password}")
    private String readerPassword;

    @Test
    void shouldAuthenticateTheReaderAndLetItRead() throws Exception {
        mvc.perform(get("/employees").with(httpBasic(readerUsername, readerPassword)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectAWrongPassword() throws Exception {
        mvc.perform(get("/employees").with(httpBasic(readerUsername, readerPassword + "-wrong")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    void shouldForbidTheReaderFromCreating() throws Exception {
        mvc.perform(post("/employees").with(httpBasic(readerUsername, readerPassword))
                        .contentType(MediaType.APPLICATION_JSON).content(EMPTY_BATCH))
                .andExpect(status().isForbidden());
    }

    // An empty batch reaches validation without creating data, which proves the admin passed security.
    @Test
    void shouldLetTheAdminReachTheEndpoint() throws Exception {
        mvc.perform(post("/employees").with(httpBasic(adminUsername, adminPassword))
                        .contentType(MediaType.APPLICATION_JSON).content(EMPTY_BATCH))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));
    }
}
