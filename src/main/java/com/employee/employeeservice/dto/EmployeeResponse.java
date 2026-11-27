package com.employee.employeeservice.dto;

import com.employee.employeeservice.domain.model.Employee;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record EmployeeResponse(
        Long id,
        String firstName,
        String middleName,
        String paternalLastName,
        String maternalLastName,
        @Schema(description = "Calculated from the birth date")
        int age,
        String sex,
        @JsonFormat(pattern = ApiDateFormat.PATTERN)
        @Schema(type = "string", pattern = ApiDateFormat.REGEX, example = ApiDateFormat.EXAMPLE)
        LocalDate birthDate,
        String jobTitle,
        @Schema(implementation = String.class, description = "Server-local ISO date and time without a UTC offset",
                pattern = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(?:\\.\\d{1,9})?$",
                example = "2026-01-15T10:30:00.123456")
        LocalDateTime createdAt,
        boolean active
) {

    public static EmployeeResponse from(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getFirstName(),
                employee.getMiddleName(),
                employee.getPaternalLastName(),
                employee.getMaternalLastName(),
                employee.getAge(),
                employee.getSex(),
                employee.getBirthDate(),
                employee.getJobTitle(),
                employee.getCreatedAt(),
                employee.isActive()
        );
    }
}