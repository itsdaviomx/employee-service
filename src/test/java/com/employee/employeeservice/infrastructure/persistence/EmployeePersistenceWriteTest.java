package com.employee.employeeservice.infrastructure.persistence;

import com.employee.employeeservice.domain.exception.InvalidEmployeeException;
import com.employee.employeeservice.domain.model.Employee;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class EmployeePersistenceWriteTest {

    @Test
    void shouldRequireRepairBeforeSavingAnIncompleteHistoricalEmployee() {
        SpringDataEmployeeRepository repository = mock(SpringDataEmployeeRepository.class);
        EmployeePersistenceAdapter adapter = new EmployeePersistenceAdapter(repository);
        Employee historical = Employee.restore(7L, null, null, "Diaz", null, "F",
                LocalDate.of(1995, 3, 14), "Developer", LocalDateTime.of(2025, 3, 14, 9, 0), true);

        assertThatThrownBy(() -> adapter.save(historical))
                .isInstanceOf(InvalidEmployeeException.class)
                .hasMessageContaining("firstName");
        verifyNoInteractions(repository);
    }
}
