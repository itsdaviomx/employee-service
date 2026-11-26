package com.employee.employeeservice.application.service;

import com.employee.employeeservice.application.port.in.CreateEmployeeCommand;
import com.employee.employeeservice.application.port.out.EmployeeRepository;
import com.employee.employeeservice.domain.model.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateEmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    private CreateEmployeeService service;

    @BeforeEach
    void setUp() {
        service = new CreateEmployeeService(employeeRepository);
    }

    @Test
    void shouldRejectMismatchedAgeBeforeSaving() {
        CreateEmployeeCommand command = createCommand(LocalDate.now(), 25);

        assertThrows(IllegalArgumentException.class, () -> service.execute(command));

        verifyNoInteractions(employeeRepository);
    }

    @ParameterizedTest
    @ValueSource(ints = 30)
    @NullSource
    void shouldReturnSavedEmployeeWithAssignedId(Integer declaredAge) {
        LocalDate birthDate = LocalDate.of(LocalDate.now().getYear() - 30, 1, 1);
        CreateEmployeeCommand command = createCommand(birthDate, declaredAge);

        Employee savedEmployee = new Employee(
                1L,
                command.firstName(),
                command.middleName(),
                command.paternalLastName(),
                command.maternalLastName(),
                command.sex(),
                command.birthDate(),
                command.jobTitle(),
                LocalDateTime.now(),
                command.active()
        );
        when(employeeRepository.save(any(Employee.class))).thenReturn(savedEmployee);

        Employee result = service.execute(command);

        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).save(captor.capture());

        Employee employeeSentToSave = captor.getValue();
        assertNull(employeeSentToSave.getId());
        assertNotNull(employeeSentToSave.getCreatedAt());
        assertEquals(command.firstName(), employeeSentToSave.getFirstName());
        assertEquals(command.birthDate(), employeeSentToSave.getBirthDate());
        assertEquals(command.jobTitle(), employeeSentToSave.getJobTitle());

        assertSame(savedEmployee, result);
    }

    private CreateEmployeeCommand createCommand(LocalDate birthDate, Integer declaredAge) {
        return new CreateEmployeeCommand(
                "Ana",
                "Maria",
                "Garcia",
                "Lopez",
                "F",
                birthDate,
                declaredAge,
                "Developer",
                true
        );
    }
}