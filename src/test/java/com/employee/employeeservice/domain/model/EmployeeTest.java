package com.employee.employeeservice.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EmployeeTest {

    @Test
    void shouldRejectMissingBirthDate() {
        assertThrows(IllegalArgumentException.class, () -> employeeBornOn(null));
    }

    @Test
    void shouldRejectFutureBirthDate() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        assertThrows(IllegalArgumentException.class, () -> employeeBornOn(tomorrow));
    }

    @Test
    void shouldAcceptZeroAgeWhenBornToday() {
        Employee employee = employeeBornOn(LocalDate.now());

        assertEquals(0, employee.getAge());
        assertDoesNotThrow(() -> employee.validateDeclaredAge(0));
    }

    @Test
    void shouldCalculateAgeInCompletedYears() {
        LocalDate birthDate = LocalDate.now().minusYears(30).plusDays(1);
        Employee employee = employeeBornOn(birthDate);

        assertEquals(29, employee.getAge());
        assertDoesNotThrow(() -> employee.validateDeclaredAge(29));
    }

    @Test
    void shouldRejectNegativeDeclaredAge() {
        Employee employee = employeeBornOn(LocalDate.now());

        assertThrows(
                IllegalArgumentException.class,
                () -> employee.validateDeclaredAge(-1)
        );
    }

    @Test
    void shouldRejectDeclaredAgeThatDoesNotMatchBirthDate() {
        Employee employee = employeeBornOn(LocalDate.now());

        assertThrows(
                IllegalArgumentException.class,
                () -> employee.validateDeclaredAge(1)
        );
    }

    private Employee employeeBornOn(LocalDate birthDate) {
        return new Employee(
                1L,
                "Ana",
                "Maria",
                "Garcia",
                "Lopez",
                "F",
                birthDate,
                "Developer",
                LocalDateTime.now(),
                true
        );
    }
}