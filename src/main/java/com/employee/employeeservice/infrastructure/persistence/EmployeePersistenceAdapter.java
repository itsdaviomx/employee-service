package com.employee.employeeservice.infrastructure.persistence;

import com.employee.employeeservice.application.port.out.EmployeeRepository;
import com.employee.employeeservice.domain.model.Employee;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class EmployeePersistenceAdapter implements EmployeeRepository {

    private final SpringDataEmployeeRepository jpaRepository;

    @Override
    public Employee save(Employee employee) {
        EmployeeEntity entity = toEntity(employee);
        EmployeeEntity savedEntity = jpaRepository.save(entity);
        return toDomain(savedEntity);
    }

    private EmployeeEntity toEntity(Employee employee) {
        return new EmployeeEntity(
                employee.getId(),
                employee.getFirstName(),
                employee.getMiddleName(),
                employee.getPaternalLastName(),
                employee.getMaternalLastName(),
                employee.getSex(),
                employee.getBirthDate(),
                employee.getJobTitle(),
                employee.getCreatedAt(),
                employee.isActive()
        );
    }

    private Employee toDomain(EmployeeEntity entity) {
        return new Employee(
                entity.getId(),
                entity.getFirstName(),
                entity.getMiddleName(),
                entity.getPaternalLastName(),
                entity.getMaternalLastName(),
                entity.getSex(),
                entity.getBirthDate(),
                entity.getJobTitle(),
                entity.getCreatedAt(),
                entity.isActive()
        );
    }
}