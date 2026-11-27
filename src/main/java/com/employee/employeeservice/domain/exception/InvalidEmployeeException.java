package com.employee.employeeservice.domain.exception;

/**
 * A business rule of {@code Employee} was broken.
 *
 * <p>It extends {@link RuntimeException} on purpose: Spring translates {@link IllegalArgumentException}
 * thrown inside a {@code @Repository} into a data access exception, and the update rules run
 * inside the persistence adapter's transaction.
 */
public class InvalidEmployeeException extends RuntimeException {

    public InvalidEmployeeException(String message) {
        super(message);
    }
}
