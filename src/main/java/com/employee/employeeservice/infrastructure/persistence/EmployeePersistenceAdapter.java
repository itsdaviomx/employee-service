package com.employee.employeeservice.infrastructure.persistence;

import com.employee.employeeservice.application.port.out.EmployeeRepository;
import com.employee.employeeservice.domain.model.Employee;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.UnaryOperator;

@Repository
@RequiredArgsConstructor
public class EmployeePersistenceAdapter implements EmployeeRepository {

    // Must match the ESCAPE '!' clause in SpringDataEmployeeRepository.
    private static final String LIKE_ESCAPE = "!";
    private static final String LIKE_ANY = "%";
    private static final String LIKE_SINGLE = "_";

    private final SpringDataEmployeeRepository jpaRepository;

    @Override
    public Employee save(Employee employee) {
        employee.validateForWrite();
        EmployeeEntity entity = toEntity(employee);
        EmployeeEntity savedEntity = jpaRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<Employee> findById(long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional
    public Optional<Employee> update(long id, UnaryOperator<Employee> change) {
        return jpaRepository.findByIdForUpdate(id)
                .map(this::toDomain)
                .map(change)
                .map(this::save);
    }

    @Override
    public List<Employee> findAll() {
        return jpaRepository.findAll(Sort.by("id").ascending()).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Employee> searchByName(String name) {
        return jpaRepository.searchByNamePattern(containsPattern(name)).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean deleteById(long id) {
        return jpaRepository.deleteEmployeeById(id) > 0;
    }

    private static String containsPattern(String text) {
        String literal = text.toLowerCase(Locale.ROOT)
                .replace(LIKE_ESCAPE, LIKE_ESCAPE + LIKE_ESCAPE)
                .replace(LIKE_ANY, LIKE_ESCAPE + LIKE_ANY)
                .replace(LIKE_SINGLE, LIKE_ESCAPE + LIKE_SINGLE);
        return LIKE_ANY + literal + LIKE_ANY;
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
        return Employee.restore(
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
