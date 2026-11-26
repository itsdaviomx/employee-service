package com.employee.employeeservice.application.port.out;

import com.employee.employeeservice.domain.model.Employee;

import java.util.Optional;

public interface EmployeeRepository {

    Employee save(Employee employee);

    Optional<Employee> findById(long id);
}
