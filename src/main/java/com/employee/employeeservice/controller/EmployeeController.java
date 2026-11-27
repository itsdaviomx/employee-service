package com.employee.employeeservice.controller;

import com.employee.employeeservice.application.service.DeleteEmployeeService;
import com.employee.employeeservice.application.service.UpdateEmployeeService;
import com.employee.employeeservice.infrastructure.idempotency.IdempotentEmployeeCreator;
import com.employee.employeeservice.dto.CreateEmployeeRequest;
import com.employee.employeeservice.dto.CreateEmployeesRequest;
import com.employee.employeeservice.dto.EmployeeCreationResult;
import com.employee.employeeservice.dto.EmployeeResponse;
import com.employee.employeeservice.dto.ErrorResponse;
import com.employee.employeeservice.dto.UpdateEmployeeRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.Validator;
import java.sql.SQLException;
import java.sql.SQLTransientConnectionException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Tag(name = "Employees")
@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final IdempotentEmployeeCreator creator;
    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final DeleteEmployeeService deleteEmployeeService;
    private final UpdateEmployeeService updateEmployeeService;

    @Operation(
            summary = "Create one or more employees",
            description = "Each item is validated and saved on its own and the response reports every item by position. "
                    + "Send up to 100 items per request (1 MiB total). A requestId protects retries for 24 hours."
    )
    @ApiResponse(responseCode = "201", description = "Every employee was created")
    @ApiResponse(responseCode = "200", description = "Some items were not created; check each item's status")
    @ApiResponse(responseCode = "400", description = "Unreadable body or batch size outside the range of 1 to 100",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "413", description = "Request body exceeds 1 MiB",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "503", description = "Database unavailable for every item",
            content = @Content(array = @ArraySchema(
                    schema = @Schema(implementation = EmployeeCreationResult.class))))
    @PostMapping
    public ResponseEntity<List<EmployeeCreationResult>> create(
            @Valid @RequestBody CreateEmployeesRequest request
    ) {
        List<EmployeeCreationResult> results = new ArrayList<>();

        int unavailable = 0;
        for (int index = 0; index < request.employees().size(); index++) {
            JsonNode item = request.employees().get(index);
            int position = index + 1;
            try {
                results.add(createOne(position, item));
            } catch (DataAccessException | TransactionException exception) {
                log.error("Persistence failure for employee at batch position {}", position, exception);
                if (isDatabaseUnavailable(exception)) {
                    unavailable++;
                }
                results.add(EmployeeCreationResult.persistenceError(position));
            }
        }

        if (unavailable == results.size()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(results);
        }
        boolean allCreated = results.stream()
                .allMatch(result -> result.status() == EmployeeCreationResult.Status.CREATED);

        return ResponseEntity.status(allCreated ? HttpStatus.CREATED : HttpStatus.OK)
                .body(results);
    }

    @Operation(
            summary = "Update all or some fields of an employee",
            description = "Omitted or null fields keep their current value. ID and creation date never change."
    )
    @ApiResponse(responseCode = "200", description = "Employee updated")
    @ApiResponse(responseCode = "400", description = "Invalid field, wrong date format or age that does not match",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Employee not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PutMapping("/{id}")
    public EmployeeResponse update(
            @PathVariable("id") long id,
            @Valid @RequestBody UpdateEmployeeRequest request
    ) {
        return EmployeeResponse.from(updateEmployeeService.execute(id, request.toCommand()));
    }

    @Operation(summary = "Delete an employee by ID")
    @ApiResponse(responseCode = "204", description = "Employee deleted")
    @ApiResponse(responseCode = "400", description = "ID is not a number",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Employee not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") long id) {
        deleteEmployeeService.execute(id);
    }

    private EmployeeCreationResult createOne(int position, JsonNode item) {
        if (item == null || !item.isObject()) {
            return EmployeeCreationResult.invalid(position, "Employee must be a JSON object");
        }

        CreateEmployeeRequest request;
        try {
            request = objectMapper.treeToValue(item, CreateEmployeeRequest.class);
        } catch (JsonProcessingException exception) {
            return EmployeeCreationResult.invalid(
                    position,
                    "Invalid field format. Check field types and use dd-MM-yyyy for birthDate"
            );
        }

        String validationErrors = validator.validate(request).stream()
                .map(error -> error.getPropertyPath() + ": " + error.getMessage())
                .sorted()
                .collect(Collectors.joining("; "));

        if (!validationErrors.isEmpty()) {
            return EmployeeCreationResult.invalid(position, validationErrors);
        }

        try {
            var outcome = creator.create(request.requestId(), request.toCommand());
            if (outcome.replayed()) {
                return EmployeeCreationResult.replayed(position, outcome.employee());
            }
            return EmployeeCreationResult.created(position, outcome.employee());
        } catch (IdempotentEmployeeCreator.RequestIdConflict exception) {
            return EmployeeCreationResult.conflict(position, exception.getMessage());
        } catch (IllegalArgumentException exception) {
            return EmployeeCreationResult.invalid(position, exception.getMessage());
        }
    }

    private static boolean isDatabaseUnavailable(Throwable error) {
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (cause instanceof DataAccessResourceFailureException
                    || cause instanceof TransientDataAccessResourceException
                    || cause instanceof SQLTransientConnectionException
                    || cause instanceof SQLException sql && sql.getSQLState() != null && sql.getSQLState().startsWith("08")) {
                return true;
            }
        }
        return false;
    }
}
