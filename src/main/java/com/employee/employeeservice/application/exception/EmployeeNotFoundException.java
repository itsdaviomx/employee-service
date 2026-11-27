package com.employee.employeeservice.application.exception;

public class EmployeeNotFoundException extends RuntimeException {

    public EmployeeNotFoundException(long id) {
        super("Employee with ID " + id + " was not found");
    }
}
