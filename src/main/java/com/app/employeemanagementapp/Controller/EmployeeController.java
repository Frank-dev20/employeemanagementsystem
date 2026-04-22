package com.app.employeemanagementapp.Controller;

import com.app.employeemanagementapp.DTO.EmployeeRequestDto;
import com.app.employeemanagementapp.DTO.EmployeeResponseDTO;
import com.app.employeemanagementapp.DTO.ImportResultDto;
import com.app.employeemanagementapp.Entity.Employee;
import com.app.employeemanagementapp.Mapper.EmployeeMapper;
import com.app.employeemanagementapp.Service.EmployeeService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

import static com.app.employeemanagementapp.Mapper.EmployeeMapper.mapToDto;
import static com.app.employeemanagementapp.Mapper.EmployeeMapper.mapToEntity;

@RestController
@RequestMapping("/api/v1/employee")
public class EmployeeController {
    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService){
        this.employeeService = employeeService;
    }

    //Create
    @PostMapping
    public EmployeeResponseDTO create(@Valid @RequestBody EmployeeRequestDto employeeRequestDto){
        Employee createdEmployee = employeeService.createEmployee(mapToEntity(employeeRequestDto));
        return mapToDto(createdEmployee);
    }
    //Get ALL WITH PAGINATION
    @GetMapping
    public Page<EmployeeResponseDTO> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10")int size){
        Page<Employee> employees = new PageImpl<>(employeeService.getAllEmployees());

        return employees.map(EmployeeMapper::mapToDto);
    }


    //Get by ID
    @GetMapping("/{id}")
    public EmployeeResponseDTO getById(@PathVariable Long id){
        return mapToDto(employeeService.getEmployeeById(id));
    }

    //Full Update by Id
    @PutMapping("/{id}")
    public EmployeeResponseDTO update(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequestDto employeeRequestDto){
        Employee updatedEmployee = employeeService.updateEmployees(mapToEntity(employeeRequestDto), id);
        return mapToDto(updatedEmployee);
    }

    //Partial Update page
    @PatchMapping("/{id}")
    public EmployeeResponseDTO partialUpdate(@PathVariable Long id,
                                             @RequestParam(required = false)BigDecimal salary,
                                             @RequestParam(required = false)String department,
                                             @RequestParam(required = false)Boolean active){
        return mapToDto(employeeService.partialUpdate(id, salary, department, active));
    }

    // Soft Delete
    @DeleteMapping("/{id}")
    public void softDelete(@PathVariable Long id){
        employeeService.softDelete(id);
    }

    //Hard Delete
    @DeleteMapping("/{id}/hard")
    public void hardDelete(@PathVariable Long id){
        employeeService.hardDelete(id);
    }

    // Salary Range
    @GetMapping("/salary-range")
    public List<EmployeeResponseDTO> salaryRange(@RequestParam BigDecimal min,
                                                 @RequestParam BigDecimal max){
        return employeeService.getBySalaryRange(min, max)
                .stream()
                .map(EmployeeMapper::mapToDto)
                .toList();
    }


    @PostMapping(value = "/import", consumes = "multipart/form-data")
    public ImportResultDto importFromExcel(@RequestParam("file") MultipartFile file){
//        System.out.println("Endpoint hit!");
//        System.out.println("File received: " + file.getOriginalFilename());
        return employeeService.importFromExcel(file);
    }

//    @PostMapping("/import")
//    public String test() {
//        System.out.println("Endpoint hit!");
//        return "OK";
//    }

    @GetMapping("/export/excel")
    public void exportExcel(
            HttpServletResponse response,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) Boolean active){
        System.out.println("exporting");
        employeeService.exportToExcel(response, department, active);

    }

    @GetMapping("/export/pdf")
    public void exportPdf(HttpServletResponse response) {
        employeeService.exportToPdf(response);
    }
}
