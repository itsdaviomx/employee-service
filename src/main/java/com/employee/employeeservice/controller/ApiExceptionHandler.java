package com.employee.employeeservice.controller;

import com.employee.employeeservice.application.exception.EmployeeNotFoundException;
import com.employee.employeeservice.application.exception.InvalidSearchTermException;
import com.employee.employeeservice.domain.exception.InvalidEmployeeException;
import com.employee.employeeservice.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final String EMPLOYEE_NOT_FOUND = "EMPLOYEE_NOT_FOUND";
    private static final String INVALID_EMPLOYEE = "INVALID_EMPLOYEE";
    private static final String INVALID_SEARCH_TERM = "INVALID_SEARCH_TERM";
    private static final String MISSING_PARAMETER = "MISSING_PARAMETER";
    private static final String VALIDATION_FAILED = "VALIDATION_FAILED";
    private static final String MALFORMED_REQUEST = "MALFORMED_REQUEST";
    private static final String INVALID_PARAMETER = "INVALID_PARAMETER";

    @ExceptionHandler(EmployeeNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse employeeNotFound(EmployeeNotFoundException exception) {
        return new ErrorResponse(EMPLOYEE_NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(InvalidEmployeeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse invalidEmployee(InvalidEmployeeException exception) {
        return new ErrorResponse(INVALID_EMPLOYEE, exception.getMessage());
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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse validationFailed(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining("; "));
        return new ErrorResponse(VALIDATION_FAILED, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse malformedRequest(HttpMessageNotReadableException exception) {
        return new ErrorResponse(
                MALFORMED_REQUEST,
                "Request body is missing or malformed. Check field types and use dd-MM-yyyy for birthDate"
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse invalidParameter(MethodArgumentTypeMismatchException exception) {
        return new ErrorResponse(
                INVALID_PARAMETER,
                "Parameter '" + exception.getName() + "' has an invalid value"
        );
    }
}
