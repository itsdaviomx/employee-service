package com.employee.employeeservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EmployeeCreationResult(
        int position,
        Status status,
        EmployeeResponse employee,
        String error
) {
    public enum Status {
        CREATED,
        REPLAYED,
        CONFLICT,
        INVALID,
        PERSISTENCE_ERROR
    }

    public static EmployeeCreationResult created(int position, EmployeeResponse employee) {
        return new EmployeeCreationResult(position, Status.CREATED, employee, null);
    }

    public static EmployeeCreationResult replayed(int position, EmployeeResponse employee) {
        return new EmployeeCreationResult(position, Status.REPLAYED, employee, null);
    }

    public static EmployeeCreationResult conflict(int position, String error) {
        return new EmployeeCreationResult(position, Status.CONFLICT, null, error);
    }

    public static EmployeeCreationResult invalid(int position, String error) {
        return new EmployeeCreationResult(position, Status.INVALID, null, error);
    }

    public static EmployeeCreationResult persistenceError(int position) {
        return new EmployeeCreationResult(
                position,
                Status.PERSISTENCE_ERROR,
                null,
                "Could not confirm that the employee was saved"
        );
    }
}
