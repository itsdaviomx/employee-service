package com.employee.employeeservice.dto;

import com.fasterxml.jackson.databind.JsonNode;

import javax.validation.constraints.NotEmpty;
import java.util.List;

public record CreateEmployeesRequest(
        @NotEmpty List<JsonNode> employees
) {
}