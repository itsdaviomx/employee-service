package com.employee.employeeservice.application.service;

import com.employee.employeeservice.application.exception.EmployeeNotFoundException;
import com.employee.employeeservice.application.port.out.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteEmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    private DeleteEmployeeService service;

    @BeforeEach
    void setUp() {
        service = new DeleteEmployeeService(employeeRepository);
    }

    @Test
    void shouldDeleteAnExistingEmployee() {
        when(employeeRepository.deleteById(7L)).thenReturn(true);

        assertThatCode(() -> service.execute(7L)).doesNotThrowAnyException();

        verify(employeeRepository).deleteById(7L);
    }

    @Test
    void shouldReportNotFoundWhenNothingWasDeleted() {
        when(employeeRepository.deleteById(7L)).thenReturn(false);

        assertThatThrownBy(() -> service.execute(7L))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessage("Employee with ID 7 was not found");
    }
}
