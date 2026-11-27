package com.employee.employeeservice.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotEmpty;
import java.util.List;

public record CreateEmployeesRequest(
        // Items stay as raw JSON so one malformed item cannot reject the whole batch.
        @NotEmpty
        @ArraySchema(schema = @Schema(implementation = CreateEmployeeRequest.class))
        List<JsonNode> employees
) {
}