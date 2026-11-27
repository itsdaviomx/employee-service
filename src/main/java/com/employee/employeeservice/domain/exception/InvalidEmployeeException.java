package com.employee.employeeservice.domain.exception;

public class InvalidEmployeeException extends IllegalArgumentException {

    public InvalidEmployeeException(String message) {
        super(message);
    }
}
