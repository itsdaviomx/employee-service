package com.employee.employeeservice.infrastructure.persistence;

import com.employee.employeeservice.domain.model.Employee;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(EmployeePersistenceAdapter.class)
class EmployeePersistenceAdapterTest {

    @Autowired
    private EmployeePersistenceAdapter adapter;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldSaveEmployeeAndReturnGeneratedId() {
        Employee employee = new Employee(
                null,
                "Ana",
                "Maria",
                "Garcia",
                "Lopez",
                "F",
                LocalDate.of(1995, 3, 14),
                "Developer",
                LocalDateTime.of(2026, 1, 15, 10, 30),
                true
        );

        Employee saved = adapter.save(employee);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(employee);

        entityManager.flush();
        entityManager.clear();

        EmployeeEntity stored = entityManager.find(EmployeeEntity.class, saved.getId());

        assertThat(stored).isNotNull();
        assertThat(stored.getFirstName()).isEqualTo(employee.getFirstName());
        assertThat(stored.getMiddleName()).isEqualTo(employee.getMiddleName());
        assertThat(stored.getPaternalLastName()).isEqualTo(employee.getPaternalLastName());
        assertThat(stored.getMaternalLastName()).isEqualTo(employee.getMaternalLastName());
        assertThat(stored.getSex()).isEqualTo(employee.getSex());
        assertThat(stored.getBirthDate()).isEqualTo(employee.getBirthDate());
        assertThat(stored.getJobTitle()).isEqualTo(employee.getJobTitle());
        assertThat(stored.getCreatedAt()).isEqualTo(employee.getCreatedAt());
        assertThat(stored.isActive()).isEqualTo(employee.isActive());
    }
}