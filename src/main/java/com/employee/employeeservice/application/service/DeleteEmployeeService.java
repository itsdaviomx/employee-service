package com.employee.employeeservice.application.service;

import com.employee.employeeservice.application.exception.EmployeeNotFoundException;
import com.employee.employeeservice.application.port.out.EmployeeRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeleteEmployeeService {

    private final EmployeeRepository employeeRepository;

    public void execute(long id) {
        if (!employeeRepository.deleteById(id)) {
            throw new EmployeeNotFoundException(id);
        }
    }
}
