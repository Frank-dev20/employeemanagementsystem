package com.app.employeemanagementapp.Service;

import com.app.employeemanagementapp.DTO.ImportResultDto;
import com.app.employeemanagementapp.Entity.Employee;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

//import org.springframework.data.domain.Pageable;

public interface EmployeeService {
    Employee createEmployee(Employee employee);
    Employee getEmployeeById(Long Id);
    List<Employee> getAllEmployees();
    Employee updateEmployees(Employee employee, Long Id);
    Employee partialUpdate(Long Id, BigDecimal salary, String Department, Boolean active);
    void softDelete(Long Id);
    void hardDelete(Long Id);
    List<Employee> getByDepartment(String department);
    List<Employee> getBySalaryRange(BigDecimal min, BigDecimal max);
    ImportResultDto importFromExcel(MultipartFile file);
    void exportToExcel(HttpServletResponse response, String department, Boolean active);
    void exportToPdf(HttpServletResponse response);
//    Page<Employee> getEmployees(
//            String department,
//            Boolean active,
//            Pageable pageable
//    );
};
