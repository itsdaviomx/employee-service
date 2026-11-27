package com.employee.employeeservice.application.service;

import com.employee.employeeservice.application.port.out.EmployeeRepository;
import com.employee.employeeservice.domain.model.Employee;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ListEmployeesService {

    private final EmployeeRepository employeeRepository;

    public List<Employee> execute() {
        return employeeRepository.findAll();
    }
}
