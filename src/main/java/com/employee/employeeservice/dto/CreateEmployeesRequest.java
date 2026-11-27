package com.employee.employeeservice.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

public record CreateEmployeesRequest(
        // Items stay as raw JSON so one malformed item cannot reject the whole batch.
        @NotEmpty
        @Size(max = MAX_EMPLOYEES)
        @ArraySchema(minItems = 1, maxItems = MAX_EMPLOYEES, schema = @Schema(implementation = CreateEmployeeRequest.class))
        List<JsonNode> employees
) {
    public static final int MAX_EMPLOYEES = 100;
}
