package com.employee.employeeservice.application.port.in;

import java.time.LocalDate;

public record CreateEmployeeCommand(
        String firstName,
        String middleName,
        String paternalLastName,
        String maternalLastName,
        String sex,
        LocalDate birthDate,
        Integer declaredAge,
        String jobTitle,
        boolean active
) {
}