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