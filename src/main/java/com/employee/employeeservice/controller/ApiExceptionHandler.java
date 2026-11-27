package com.employee.employeeservice.controller;

import com.employee.employeeservice.application.exception.EmployeeNotFoundException;
import com.employee.employeeservice.application.exception.InvalidSearchTermException;
import com.employee.employeeservice.domain.exception.InvalidEmployeeException;
import com.employee.employeeservice.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.stream.Collectors;

/**
 * Answers every error with {@link ErrorResponse}. Extending {@link ResponseEntityExceptionHandler}
 * keeps the right status and headers for Spring MVC errors (405 with Allow, 415 with Accept, 404, 406...);
 * only their body changes.
 */
@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String EMPLOYEE_NOT_FOUND = "EMPLOYEE_NOT_FOUND";
    private static final String INVALID_EMPLOYEE = "INVALID_EMPLOYEE";
    private static final String INVALID_SEARCH_TERM = "INVALID_SEARCH_TERM";
    private static final String MISSING_PARAMETER = "MISSING_PARAMETER";
    private static final String VALIDATION_FAILED = "VALIDATION_FAILED";
    private static final String MALFORMED_REQUEST = "MALFORMED_REQUEST";
    private static final String INVALID_PARAMETER = "INVALID_PARAMETER";
    private static final String INTERNAL_ERROR = "INTERNAL_ERROR";

    @ExceptionHandler(EmployeeNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse employeeNotFound(EmployeeNotFoundException exception) {
        return new ErrorResponse(EMPLOYEE_NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(PessimisticLockingFailureException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse concurrentModification() {
        return new ErrorResponse("CONCURRENT_MODIFICATION", "Concurrent modification. Retry the request");
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

    @ExceptionHandler(BatchBodyAdvice.PayloadTooLarge.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public ErrorResponse payloadTooLarge() {
        return new ErrorResponse("PAYLOAD_TOO_LARGE", "Batch request body exceeds 1 MiB");
    }

    // Without this, the catch-all below would turn a deliberate status into a 500.
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> responseStatus(ResponseStatusException exception) {
        HttpStatus status = exception.getStatus();
        return ResponseEntity.status(status)
                .headers(exception.getResponseHeaders())
                .body(new ErrorResponse(status.name(), status.getReasonPhrase()));
    }

    // Last resort: the client gets no internal detail; the log keeps the exception and the correlation ID.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> unexpected(Exception exception) {
        log.error("Unexpected error", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(INTERNAL_ERROR,
                        "Unexpected error. Use the X-Correlation-Id response header to trace it"));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining("; "));
        return handleExceptionInternal(exception, new ErrorResponse(VALIDATION_FAILED, message), headers, status, request);
    }

    // The parser message can expose internal class names, so the client gets a fixed hint.
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ErrorResponse body = new ErrorResponse(MALFORMED_REQUEST,
                "Request body is missing or malformed. Check field types and use dd-MM-yyyy for birthDate");
        return handleExceptionInternal(exception, body, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException exception, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ErrorResponse body = new ErrorResponse(MISSING_PARAMETER,
                "Query parameter '" + exception.getParameterName() + "' is required");
        return handleExceptionInternal(exception, body, headers, status, request);
    }

    // The rejected value is not echoed back to avoid reflecting untrusted input.
    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException exception, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String name = exception instanceof MethodArgumentTypeMismatchException argument
                ? argument.getName()
                : exception.getPropertyName();
        ErrorResponse body = new ErrorResponse(INVALID_PARAMETER, "Parameter '" + name + "' has an invalid value");
        return handleExceptionInternal(exception, body, headers, status, request);
    }

    // Every other Spring MVC error keeps its status and headers and gets the API body.
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception exception, @Nullable Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        if (status.is5xxServerError()) {
            log.error("Server error while handling the request", exception);
        }
        Object errorBody = body != null ? body : new ErrorResponse(status.name(), status.getReasonPhrase());
        return super.handleExceptionInternal(exception, errorBody, headers, status, request);
    }
}
