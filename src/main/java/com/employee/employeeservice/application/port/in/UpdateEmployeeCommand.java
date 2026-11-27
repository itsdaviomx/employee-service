package com.employee.employeeservice.application.port.in;

import java.time.LocalDate;

public record UpdateEmployeeCommand(
        String firstName,
        String middleName,
        String paternalLastName,
        String maternalLastName,
        String sex,
        LocalDate birthDate,
        Integer declaredAge,
        String jobTitle,
        Boolean active
) {
}
