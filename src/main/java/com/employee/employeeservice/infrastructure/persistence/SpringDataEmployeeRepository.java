package com.employee.employeeservice.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpringDataEmployeeRepository
        extends JpaRepository<EmployeeEntity, Long> {

    @Query("""
            SELECT e FROM EmployeeEntity e
            WHERE LOWER(e.firstName) LIKE :pattern ESCAPE '!'
               OR LOWER(e.middleName) LIKE :pattern ESCAPE '!'
               OR LOWER(e.paternalLastName) LIKE :pattern ESCAPE '!'
               OR LOWER(e.maternalLastName) LIKE :pattern ESCAPE '!'
            ORDER BY e.id ASC
            """)
    List<EmployeeEntity> searchByNamePattern(@Param("pattern") String pattern);
}
