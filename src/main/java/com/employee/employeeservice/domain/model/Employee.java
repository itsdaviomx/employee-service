package com.employee.employeeservice.domain.model;

import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

@Getter
public final class Employee {

    private final Long id;
    private final String firstName;
    private final String middleName;
    private final String paternalLastName;
    private final String maternalLastName;
    private final String sex;
    private final LocalDate birthDate;
    private final String jobTitle;
    private final LocalDateTime createdAt;
    private final boolean active;

    public Employee(
            Long id,
            String firstName,
            String middleName,
            String paternalLastName,
            String maternalLastName,
            String sex,
            LocalDate birthDate,
            String jobTitle,
            LocalDateTime createdAt,
            boolean active
    ) {
        if (birthDate == null) {
            throw new IllegalArgumentException("Birth date is required");
        }

        if (birthDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Birth date cannot be in the future");
        }

        this.id = id;
        this.firstName = firstName;
        this.middleName = middleName;
        this.paternalLastName = paternalLastName;
        this.maternalLastName = maternalLastName;
        this.sex = sex;
        this.birthDate = birthDate;
        this.jobTitle = jobTitle;
        this.createdAt = createdAt;
        this.active = active;
    }

    public int getAge() {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    public void validateDeclaredAge(int declaredAge) {
        if (declaredAge < 0) {
            throw new IllegalArgumentException("Age must be zero or greater");
        }

        if (declaredAge != getAge()) {
            throw new IllegalArgumentException("Age must match the birth date");
        }
    }
}