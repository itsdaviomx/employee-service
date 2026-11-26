package com.employee.employeeservice.controller;

import com.employee.employeeservice.application.service.CreateEmployeeService;
import com.employee.employeeservice.domain.model.Employee;
import com.employee.employeeservice.dto.CreateEmployeeRequest;
import com.employee.employeeservice.dto.CreateEmployeesRequest;
import com.employee.employeeservice.dto.EmployeeCreationResult;
import com.employee.employeeservice.dto.EmployeeResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.Validator;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final CreateEmployeeService createEmployeeService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @PostMapping
    public ResponseEntity<List<EmployeeCreationResult>> create(
            @Valid @RequestBody CreateEmployeesRequest request
    ) {
        List<EmployeeCreationResult> results = new ArrayList<>();

        for (int index = 0; index < request.employees().size(); index++) {
            JsonNode item = request.employees().get(index);
            results.add(createOne(index + 1, item));
        }

        boolean allCreated = results.stream()
                .allMatch(result -> result.status() == EmployeeCreationResult.Status.CREATED);

        return ResponseEntity.status(allCreated ? HttpStatus.CREATED : HttpStatus.OK)
                .body(results);
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
            Employee saved = createEmployeeService.execute(request.toCommand());
            return EmployeeCreationResult.created(position, EmployeeResponse.from(saved));
        } catch (IllegalArgumentException exception) {
            return EmployeeCreationResult.invalid(position, exception.getMessage());
        } catch (DataAccessException | TransactionException exception) {
            log.error("Persistence failure for employee at batch position {}", position, exception);
            return EmployeeCreationResult.persistenceError(position);
        }
    }
}