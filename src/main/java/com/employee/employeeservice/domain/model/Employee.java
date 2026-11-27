package com.employee.employeeservice.domain.model;

import com.employee.employeeservice.domain.exception.InvalidEmployeeException;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Objects;

@Getter
public final class Employee {

    public static final int MAX_TEXT_LENGTH = 255;

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
        this(id, firstName, middleName, paternalLastName, maternalLastName, sex, birthDate, jobTitle, createdAt, active, true);
    }

    // Existing rows may predate the required-field rules. Reads and repairs must remain possible.
    public static Employee restore(Long id, String firstName, String middleName, String paternalLastName,
                                   String maternalLastName, String sex, LocalDate birthDate, String jobTitle,
                                   LocalDateTime createdAt, boolean active) {
        return new Employee(Objects.requireNonNull(id), firstName, middleName, paternalLastName, maternalLastName,
                sex, birthDate, jobTitle, createdAt, active, false);
    }

    private Employee(Long id, String firstName, String middleName, String paternalLastName, String maternalLastName,
                     String sex, LocalDate birthDate, String jobTitle, LocalDateTime createdAt, boolean active,
                     boolean validateText) {
        if (birthDate == null) {
            throw new InvalidEmployeeException("Birth date is required");
        }

        if (birthDate.isAfter(LocalDate.now())) {
            throw new InvalidEmployeeException("Birth date cannot be in the future");
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
        if (validateText) {
            validateForWrite();
        }
    }

    public void validateForWrite() {
        validateText("firstName", firstName, true);
        validateText("middleName", middleName, false);
        validateText("paternalLastName", paternalLastName, true);
        validateText("maternalLastName", maternalLastName, false);
        validateText("sex", sex, true);
        validateText("jobTitle", jobTitle, true);
    }

    private static void validateText(String field, String value, boolean required) {
        if (required && (value == null || value.isBlank())) {
            throw new InvalidEmployeeException(field + " must not be blank");
        }
        if (value != null && value.codePointCount(0, value.length()) > MAX_TEXT_LENGTH) {
            throw new InvalidEmployeeException(field + " must have at most " + MAX_TEXT_LENGTH + " characters");
        }
    }

    public int getAge() {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    public void validateDeclaredAge(int declaredAge) {
        if (declaredAge < 0) {
            throw new InvalidEmployeeException("Age must be zero or greater");
        }

        if (declaredAge != getAge()) {
            throw new InvalidEmployeeException("Age must match the birth date");
        }
    }
}
