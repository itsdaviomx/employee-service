package com.employee.employeeservice.application.service;

import com.employee.employeeservice.application.exception.InvalidSearchTermException;
import com.employee.employeeservice.application.port.out.EmployeeRepository;
import com.employee.employeeservice.domain.model.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchEmployeesServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    private SearchEmployeesService service;

    @BeforeEach
    void setUp() {
        service = new SearchEmployeesService(employeeRepository);
    }

    @Test
    void shouldSearchWithTheTrimmedNameAndReturnTheMatches() {
        Employee ana = new Employee(
                1L, "Ana", null, "Garcia", null, "F",
                LocalDate.of(1995, 3, 14), "Developer",
                LocalDateTime.of(2026, 1, 15, 10, 30), true
        );
        List<Employee> matches = List.of(ana);
        when(employeeRepository.searchByName("ana")).thenReturn(matches);

        List<Employee> result = service.execute("  ana  ");

        assertThat(result).isSameAs(matches);
        verify(employeeRepository).searchByName("ana");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t\n"})
    void shouldRejectBlankNamesWithoutQuerying(String name) {
        assertThatThrownBy(() -> service.execute(name))
                .isInstanceOf(InvalidSearchTermException.class)
                .hasMessage("Search name must not be blank");

        verifyNoInteractions(employeeRepository);
    }

    @Test
    void shouldAcceptANameAtTheMaximumLength() {
        String name = "a".repeat(SearchEmployeesService.MAX_NAME_LENGTH);
        when(employeeRepository.searchByName(name)).thenReturn(List.of());

        assertThat(service.execute(name)).isEmpty();
    }

    @Test
    void shouldRejectANameLongerThanTheMaximumWithoutQuerying() {
        String name = "a".repeat(SearchEmployeesService.MAX_NAME_LENGTH + 1);

        assertThatThrownBy(() -> service.execute(name))
                .isInstanceOf(InvalidSearchTermException.class);

        verifyNoInteractions(employeeRepository);
    }
}
