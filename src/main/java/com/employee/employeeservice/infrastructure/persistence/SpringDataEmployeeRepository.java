package com.employee.employeeservice.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataEmployeeRepository
        extends JpaRepository<EmployeeEntity, Long> {
}