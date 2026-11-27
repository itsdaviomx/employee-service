package com.employee.employeeservice.application.exception;

public class InvalidSearchTermException extends RuntimeException {

    public InvalidSearchTermException(String message) {
        super(message);
    }
}
