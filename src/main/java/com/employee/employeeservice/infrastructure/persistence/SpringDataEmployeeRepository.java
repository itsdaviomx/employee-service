package com.employee.employeeservice.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface SpringDataEmployeeRepository
        extends JpaRepository<EmployeeEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM EmployeeEntity e WHERE e.id = :id")
    Optional<EmployeeEntity> findByIdForUpdate(@Param("id") long id);

    @Query("""
            SELECT e FROM EmployeeEntity e
            WHERE LOWER(e.firstName) LIKE :pattern ESCAPE '!'
               OR LOWER(e.middleName) LIKE :pattern ESCAPE '!'
               OR LOWER(e.paternalLastName) LIKE :pattern ESCAPE '!'
               OR LOWER(e.maternalLastName) LIKE :pattern ESCAPE '!'
            ORDER BY e.id ASC
            """)
    List<EmployeeEntity> searchByNamePattern(@Param("pattern") String pattern);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM EmployeeEntity e WHERE e.id = :id")
    int deleteEmployeeById(@Param("id") long id);
}
