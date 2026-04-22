package com.app.employeemanagementapp.Mapper;

import com.app.employeemanagementapp.DTO.EmployeeRequestDto;
import com.app.employeemanagementapp.DTO.EmployeeResponseDTO;
import com.app.employeemanagementapp.Entity.Employee;

public class EmployeeMapper {
    public static Employee mapToEntity(EmployeeRequestDto employeeRequestDto) {
        return Employee.builder()
                .firstName(employeeRequestDto.getFirstName())
                .lastName(employeeRequestDto.getLastName())
                .email(employeeRequestDto.getEmail())
                .department(employeeRequestDto.getDepartment())
                .salary(employeeRequestDto.getSalary())
                .dateOfJoining(employeeRequestDto.getDateOfJoining())
                .active(employeeRequestDto.getActive())
                .build();
    }

    public static EmployeeResponseDTO mapToDto(Employee employee){
        return EmployeeResponseDTO.builder()
                .id(employee.getId())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .salary(employee.getSalary())
                .department(employee.getDepartment())
                .dateOfJoining(employee.getDateOfJoining())
                .active(employee.getActive())
                .createdAt(employee.getCreatedAt())
                .updatedAt(employee.getUpdatedAt())
                .build();
    }
}
