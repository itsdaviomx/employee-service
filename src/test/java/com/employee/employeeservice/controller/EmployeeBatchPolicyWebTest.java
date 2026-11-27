package com.employee.employeeservice.controller;

import com.employee.employeeservice.WithAdminUser;
import com.employee.employeeservice.application.service.DeleteEmployeeService;
import com.employee.employeeservice.application.service.UpdateEmployeeService;
import com.employee.employeeservice.infrastructure.idempotency.IdempotentEmployeeCreator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.CannotCreateTransactionException;

import java.sql.SQLTransientConnectionException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
@WithAdminUser
class EmployeeBatchPolicyWebTest {

    private static final String INVALID_EMPLOYEE = """
            {"firstName":"Eva","paternalLastName":"Ruiz","sex":"F","birthDate":"31-02-2000",
             "jobTitle":"Engineer","active":true}
            """;
    private static final String EMPLOYEE = """
            {"firstName":"Ana","paternalLastName":"Ruiz","sex":"F","birthDate":"01-01-2000",
             "jobTitle":"Engineer","active":true}
            """;
    private static final IdempotentEmployeeCreator.Outcome CREATED = new IdempotentEmployeeCreator.Outcome(null, false);

    @Autowired private MockMvc mvc;
    @MockBean private IdempotentEmployeeCreator creator;
    @MockBean private DeleteEmployeeService deleteService;
    @MockBean private UpdateEmployeeService updateService;

    @Test
    void anOutageStopsTheBatchAndReturns503WhenNothingWasCreated() throws Exception {
        when(creator.create(isNull(), any())).thenThrow(new CannotCreateTransactionException(
                "transaction unavailable", new SQLTransientConnectionException("connection pool timed out")));
        send(batch(3)).andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$[0].status").value("PERSISTENCE_ERROR"))
                .andExpect(jsonPath("$[1].status").value("NOT_ATTEMPTED"))
                .andExpect(jsonPath("$[2].status").value("NOT_ATTEMPTED"))
                .andExpect(jsonPath("$[2].position").value(3));
        // Only the first item waited for the outage.
        verify(creator, times(1)).create(isNull(), any());
    }

    @Test
    void anOutageAfterACreationKeepsTheCreatedItemAndReturns200() throws Exception {
        when(creator.create(isNull(), any())).thenReturn(CREATED)
                .thenThrow(new CannotGetJdbcConnectionException("offline"));
        send(batch(3)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("CREATED"))
                .andExpect(jsonPath("$[1].status").value("PERSISTENCE_ERROR"))
                .andExpect(jsonPath("$[2].status").value("NOT_ATTEMPTED"));
        verify(creator, times(2)).create(isNull(), any());
    }

    @Test
    void itemsAfterAnOutageAreStillValidated() throws Exception {
        when(creator.create(isNull(), any())).thenThrow(new CannotGetJdbcConnectionException("offline"));
        send("{\"employees\":[" + EMPLOYEE + "," + INVALID_EMPLOYEE + "," + EMPLOYEE + "]}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PERSISTENCE_ERROR"))
                .andExpect(jsonPath("$[1].status").value("INVALID"))
                .andExpect(jsonPath("$[2].status").value("NOT_ATTEMPTED"));
    }

    @Test
    void otherPersistenceFailuresDoNotStopTheBatch() throws Exception {
        when(creator.create(isNull(), any())).thenThrow(new DataIntegrityViolationException("constraint"))
                .thenReturn(CREATED);
        send(batch(2)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PERSISTENCE_ERROR"))
                .andExpect(jsonPath("$[1].status").value("CREATED"));
        verify(creator, times(2)).create(isNull(), any());
    }

    @Test
    void rejectsOversizedBatchBeforeSavingAndAcceptsExactly100Employees() throws Exception {
        send(batch(101)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));
        verifyNoInteractions(creator);
        when(creator.create(isNull(), any())).thenReturn(CREATED);
        send(batch(100)).andExpect(status().isCreated())
                .andExpect(jsonPath("$[99].status").value("CREATED"));
    }

    @Test
    void checksTheByteLimitBeforeParsingJson() throws Exception {
        String body = batch(1);
        String atLimit = body + " ".repeat(BatchBodyAdvice.MAX_BYTES - body.length());
        send(atLimit + " ").andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.error").value("PAYLOAD_TOO_LARGE"));
        verifyNoInteractions(creator);
        when(creator.create(isNull(), any())).thenReturn(CREATED);
        send(atLimit).andExpect(status().isCreated());
    }

    @Test
    void alsoBoundsTheInputWhenContentLengthIsUnknown() {
        MockHttpInputMessage input = new MockHttpInputMessage(new byte[BatchBodyAdvice.MAX_BYTES + 1]);
        assertThatThrownBy(() -> new BatchBodyAdvice().beforeBodyRead(input, null, null, null))
                .isInstanceOf(BatchBodyAdvice.PayloadTooLarge.class);
    }

    @Test
    void returns409WhenTheUpdateCannotAcquireItsLock() throws Exception {
        when(updateService.execute(anyLong(), any())).thenThrow(new CannotAcquireLockException("timeout"));
        mvc.perform(put("/employees/7").contentType(MediaType.APPLICATION_JSON).content("{\"active\":false}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONCURRENT_MODIFICATION"));
    }

    private static String batch(int count) {
        return "{\"employees\":[" + String.join(",", Collections.nCopies(count, EMPLOYEE)) + "]}";
    }

    private ResultActions send(String body) throws Exception {
        return mvc.perform(post("/employees").contentType(MediaType.APPLICATION_JSON).content(body));
    }
}
