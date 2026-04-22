package com.app.employeemanagementapp.Repository;

import com.app.employeemanagementapp.Entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByDepartment(String department);
//    Page<Employee> findByDepartmentAndActive(String department, Boolean active, Pageable pageable);
//
//    Page<Employee> findByDepartment(String department, Pageable pageable);
//
//    Page<Employee> findByActive(Boolean active, Pageable pageable);

    Optional<Employee> findByEmail(String email);
    List<Employee> findByActiveTrue();
    @Query("SELECT e FROM Employee e WHERE e.salary BETWEEN :min AND :max")
    List<Employee> findBySalaryRange(@Param("min") BigDecimal min,
                                     @Param("max") BigDecimal max);
}
