package com.employee.employeeservice.application.port.out;

import com.employee.employeeservice.domain.model.Employee;

import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

public interface EmployeeRepository {

    Employee save(Employee employee);

    Optional<Employee> findById(long id);

    /** Applies the change to the current employee atomically. */
    Optional<Employee> update(long id, UnaryOperator<Employee> change);

    List<Employee> findAll();

    List<Employee> searchByName(String name);

    boolean deleteById(long id);
}
