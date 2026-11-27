package com.employee.employeeservice.application.service;

import com.employee.employeeservice.application.port.in.CreateEmployeeCommand;
import com.employee.employeeservice.application.port.out.EmployeeRepository;
import com.employee.employeeservice.domain.model.Employee;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class CreateEmployeeService {

    private final EmployeeRepository employeeRepository;

    public CreateEmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public Employee execute(CreateEmployeeCommand command) {
        Employee employee = new Employee(
                null,
                command.firstName(),
                command.middleName(),
                command.paternalLastName(),
                command.maternalLastName(),
                command.sex(),
                command.birthDate(),
                command.jobTitle(),
                LocalDateTime.now().truncatedTo(ChronoUnit.MICROS),
                command.active()
        );

        if (command.declaredAge() != null) {
            employee.validateDeclaredAge(command.declaredAge());
        }

        return employeeRepository.save(employee);
    }
}