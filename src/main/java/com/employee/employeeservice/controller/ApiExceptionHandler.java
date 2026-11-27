package com.employee.employeeservice.controller;

import com.employee.employeeservice.application.exception.EmployeeNotFoundException;
import com.employee.employeeservice.application.exception.InvalidSearchTermException;
import com.employee.employeeservice.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final String EMPLOYEE_NOT_FOUND = "EMPLOYEE_NOT_FOUND";
    private static final String INVALID_SEARCH_TERM = "INVALID_SEARCH_TERM";
    private static final String MISSING_PARAMETER = "MISSING_PARAMETER";

    @ExceptionHandler(EmployeeNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse employeeNotFound(EmployeeNotFoundException exception) {
        return new ErrorResponse(EMPLOYEE_NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(InvalidSearchTermException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse invalidSearchTerm(InvalidSearchTermException exception) {
        return new ErrorResponse(INVALID_SEARCH_TERM, exception.getMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse missingParameter(MissingServletRequestParameterException exception) {
        return new ErrorResponse(
                MISSING_PARAMETER,
                "Query parameter '" + exception.getParameterName() + "' is required"
        );
    }
}
