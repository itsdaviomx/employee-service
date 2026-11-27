package com.employee.employeeservice.dto;

public final class ApiDateFormat {

    public static final String PATTERN = "dd-MM-yyyy";
    public static final String REGEX = "^\\d{2}-\\d{2}-\\d{4}$";
    public static final String EXAMPLE = "14-03-1995";

    private ApiDateFormat() {
    }
}
