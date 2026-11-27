package com.employee.employeeservice.dto;

public record ErrorResponse(
        String error,
        String message
) {
}
