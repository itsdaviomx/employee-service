package com.employee.employeeservice.controller;

import com.employee.employeeservice.application.exception.EmployeeNotFoundException;
import com.employee.employeeservice.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final String EMPLOYEE_NOT_FOUND = "EMPLOYEE_NOT_FOUND";

    @ExceptionHandler(EmployeeNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse employeeNotFound(EmployeeNotFoundException exception) {
        return new ErrorResponse(EMPLOYEE_NOT_FOUND, exception.getMessage());
    }
}
