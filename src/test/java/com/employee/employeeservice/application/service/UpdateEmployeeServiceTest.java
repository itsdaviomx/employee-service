package com.employee.employeeservice.application.service;

import com.employee.employeeservice.application.exception.EmployeeNotFoundException;
import com.employee.employeeservice.application.port.in.UpdateEmployeeCommand;
import com.employee.employeeservice.application.port.out.EmployeeRepository;
import com.employee.employeeservice.domain.exception.InvalidEmployeeException;
import com.employee.employeeservice.domain.model.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.UnaryOperator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateEmployeeServiceTest {

    private static final long ID = 7L;
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 1, 15, 10, 30);

    @Mock
    private EmployeeRepository employeeRepository;

    private UpdateEmployeeService service;

    @BeforeEach
    void setUp() {
        service = new UpdateEmployeeService(employeeRepository);
    }

    private void givenCurrentEmployee() {
        when(employeeRepository.update(eq(ID), any())).thenAnswer(call -> {
            UnaryOperator<Employee> change = call.getArgument(1);
            return Optional.of(change.apply(currentEmployee()));
        });
    }

    @Test
    void shouldChangeOnlyTheProvidedFieldsAndKeepIdAndCreationDate() {
        givenCurrentEmployee();

        UpdateEmployeeCommand command = new UpdateEmployeeCommand(
                null, null, null, null, null, null, null, "Architect", false
        );

        Employee saved = service.execute(ID, command);

        assertThat(saved.getId()).isEqualTo(ID);
        assertThat(saved.getCreatedAt()).isEqualTo(CREATED_AT);
        assertThat(saved.getJobTitle()).isEqualTo("Architect");
        assertThat(saved.isActive()).isFalse();
        assertThat(saved)
                .usingRecursiveComparison()
                .ignoringFields("jobTitle", "active")
                .isEqualTo(currentEmployee());
    }

    @Test
    void shouldReplaceEveryFieldWhenAllAreProvided() {
        givenCurrentEmployee();
        LocalDate birthDate = LocalDate.of(1990, 6, 1);

        UpdateEmployeeCommand command = new UpdateEmployeeCommand(
                "Eva", "Sofia", "Ruiz", "Perez", "F", birthDate, null, "Manager", false
        );

        Employee saved = service.execute(ID, command);

        assertThat(saved.getFirstName()).isEqualTo("Eva");
        assertThat(saved.getMiddleName()).isEqualTo("Sofia");
        assertThat(saved.getPaternalLastName()).isEqualTo("Ruiz");
        assertThat(saved.getMaternalLastName()).isEqualTo("Perez");
        assertThat(saved.getBirthDate()).isEqualTo(birthDate);
        assertThat(saved.getJobTitle()).isEqualTo("Manager");
        assertThat(saved.isActive()).isFalse();
        assertThat(saved.getCreatedAt()).isEqualTo(CREATED_AT);
    }

    @Test
    void shouldReportNotFoundWithoutSaving() {
        when(employeeRepository.update(eq(ID), any())).thenReturn(Optional.empty());
        UpdateEmployeeCommand command = new UpdateEmployeeCommand(
                "Eva", null, null, null, null, null, null, null, null
        );

        assertThatThrownBy(() -> service.execute(ID, command))
                .isInstanceOf(EmployeeNotFoundException.class);

        verify(employeeRepository, never()).save(any());
    }

    @Test
    void shouldRejectADeclaredAgeThatDoesNotMatchTheResultingBirthDate() {
        givenCurrentEmployee();
        UpdateEmployeeCommand command = new UpdateEmployeeCommand(
                null, null, null, null, null, null, 1, null, null
        );

        assertThatThrownBy(() -> service.execute(ID, command))
                .isInstanceOf(InvalidEmployeeException.class)
                .hasMessage("Age must match the birth date");

        verify(employeeRepository, never()).save(any());
    }

    @Test
    void canReadAndRepairAnIncompleteLegacyEmployee() {
        Employee legacy = Employee.restore(ID, null, null, "Ruiz", null, "F", LocalDate.of(1995, 3, 14),
                "Engineer", CREATED_AT, true);
        when(employeeRepository.update(eq(ID), any())).thenAnswer(call -> {
            UnaryOperator<Employee> change = call.getArgument(1);
            return Optional.of(change.apply(legacy));
        });
        assertThat(legacy.getFirstName()).isNull();
        assertThatThrownBy(() -> service.execute(ID, new UpdateEmployeeCommand(
                null, null, null, null, null, null, null, null, false)))
                .isInstanceOf(InvalidEmployeeException.class);
        Employee repaired = service.execute(ID, new UpdateEmployeeCommand(
                "Ana", null, null, null, null, null, null, null, false));
        assertThat(repaired.getFirstName()).isEqualTo("Ana");
        assertThat(repaired.getCreatedAt()).isEqualTo(CREATED_AT);
    }

    private static Employee currentEmployee() {
        return new Employee(
                ID, "Ana", "Maria", "Garcia", "Lopez", "F",
                LocalDate.of(1995, 3, 14), "Developer", CREATED_AT, true
        );
    }
}
