package com.employee.employeeservice.application.port.out;

import com.employee.employeeservice.domain.model.Employee;

public interface EmployeeRepository {

    Employee save(Employee employee);
}