package com.employee.employeeservice.dto;

import com.employee.employeeservice.domain.model.Employee;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record EmployeeResponse(
        Long id,
        String firstName,
        String middleName,
        String paternalLastName,
        String maternalLastName,
        int age,
        String sex,
        @JsonFormat(pattern = "dd-MM-yyyy")
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