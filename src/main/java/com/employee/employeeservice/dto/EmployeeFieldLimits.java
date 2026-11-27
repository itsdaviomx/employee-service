package com.employee.employeeservice.dto;

public final class EmployeeFieldLimits {

    // All employee text columns are VARCHAR(255) in the MySQL schema.
    public static final int MAX_TEXT_LENGTH = 255;

    private EmployeeFieldLimits() {
    }
}
