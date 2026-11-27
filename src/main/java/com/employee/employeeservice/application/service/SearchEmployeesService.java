package com.employee.employeeservice.application.service;

import com.employee.employeeservice.application.exception.InvalidSearchTermException;
import com.employee.employeeservice.application.port.out.EmployeeRepository;
import com.employee.employeeservice.domain.model.Employee;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@RequiredArgsConstructor
public class SearchEmployeesService {

    public static final int MAX_NAME_LENGTH = 100;

    private final EmployeeRepository employeeRepository;

    public List<Employee> execute(String name) {
        String term = Optional.ofNullable(name)
                .map(String::strip)
                .filter(Predicate.not(String::isEmpty))
                .orElseThrow(() -> new InvalidSearchTermException("Search name must not be blank"));

        if (term.length() > MAX_NAME_LENGTH) {
            throw new InvalidSearchTermException(
                    "Search name must have at most " + MAX_NAME_LENGTH + " characters"
            );
        }

        return employeeRepository.searchByName(term);
    }
}
