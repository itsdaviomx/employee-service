package com.employee.employeeservice.application.service;

import com.employee.employeeservice.application.exception.EmployeeNotFoundException;
import com.employee.employeeservice.application.port.in.UpdateEmployeeCommand;
import com.employee.employeeservice.application.port.out.EmployeeRepository;
import com.employee.employeeservice.domain.model.Employee;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UpdateEmployeeService {

    private final EmployeeRepository employeeRepository;

    public Employee execute(long id, UpdateEmployeeCommand command) {
        Employee current = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));

        Employee updated = new Employee(
                current.getId(),
                valueOrCurrent(command.firstName(), current.getFirstName()),
                valueOrCurrent(command.middleName(), current.getMiddleName()),
                valueOrCurrent(command.paternalLastName(), current.getPaternalLastName()),
                valueOrCurrent(command.maternalLastName(), current.getMaternalLastName()),
                valueOrCurrent(command.sex(), current.getSex()),
                valueOrCurrent(command.birthDate(), current.getBirthDate()),
                valueOrCurrent(command.jobTitle(), current.getJobTitle()),
                current.getCreatedAt(),
                valueOrCurrent(command.active(), current.isActive())
        );

        if (command.declaredAge() != null) {
            updated.validateDeclaredAge(command.declaredAge());
        }

        return employeeRepository.save(updated);
    }

    private static <T> T valueOrCurrent(T requested, T current) {
        return requested != null ? requested : current;
    }
}
