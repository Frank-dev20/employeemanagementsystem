package com.app.employeemanagementapp.Service;

import com.app.employeemanagementapp.DTO.EmployeeRequestDto;
import com.app.employeemanagementapp.DTO.ImportResultDto;
import com.app.employeemanagementapp.Entity.Employee;
import com.app.employeemanagementapp.Exceptions.DuplicateEmailException;
import com.app.employeemanagementapp.Exceptions.EmployeeNotFoundException;
import com.app.employeemanagementapp.Exceptions.ExcelProcessingException;
import com.app.employeemanagementapp.Exceptions.InvalidFileFormatException;
import com.app.employeemanagementapp.Repository.EmployeeRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openpdf.text.*;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.app.employeemanagementapp.Mapper.EmployeeMapper.mapToEntity;

//import org.springframework.data.domain.Pageable;

@Service
public class EmployeeServiceImp implements EmployeeService{
    private final EmployeeRepository repository;
    public EmployeeServiceImp(EmployeeRepository repository){
        this.repository = repository;
    }

    @Override
    public Employee createEmployee(@Valid Employee employee){
        repository.findByEmail(employee.getEmail()).ifPresent(e->{
            throw new DuplicateEmailException("Email already exists");
        });
        validateSalary(employee.getDepartment(), employee.getSalary());
        return repository.save(employee);
    }

    public Employee getEmployeeById(Long Id){
        return repository.findById(Id).orElseThrow(()->new EmployeeNotFoundException("Employee not found"));
    }

    @Override
    public List<Employee> getAllEmployees() {
        return repository.findAll();
    }

    public Employee updateEmployees(Employee updatedEmployee, Long id){
        Employee existingEmployee = getEmployeeById(id);

        //chek for duplicate email
        repository.findByEmail(updatedEmployee.getEmail())
                .ifPresent(e->{
                    if(!e.getId().equals(id)){
                        throw new DuplicateEmailException("Email already exists");
                    }
                });
        validateSalary(updatedEmployee.getDepartment(), updatedEmployee.getSalary());

        existingEmployee.setFirstName(updatedEmployee.getFirstName());
        existingEmployee.setLastName(updatedEmployee.getLastName());
        existingEmployee.setEmail(updatedEmployee.getEmail());
        existingEmployee.setDepartment(updatedEmployee.getDepartment());
        existingEmployee.setSalary(updatedEmployee.getSalary());
        existingEmployee.setDateOfJoining(updatedEmployee.getDateOfJoining());
        existingEmployee.setActive(updatedEmployee.getActive());

        return repository.save(existingEmployee);
    }

    public Employee partialUpdate(Long id, BigDecimal salary, String department, Boolean active){
        Employee employee = getEmployeeById(id);
        if(salary != null){
            validateSalary(employee.getDepartment(), salary);
            employee.setSalary(salary);
        }
        if(department !=null){
            validateSalary(employee.getDepartment(), salary);
            employee.setDepartment(department);
        }
        if(active !=null){
            employee.setActive(active);
        }
        return repository.save(employee);
    }

    public void softDelete(Long id){
        Employee employee = getEmployeeById(id);
        employee.setActive(false);
        repository.save(employee);
    }

    public void hardDelete(Long id){
        Employee employee = getEmployeeById(id);
        if(employee.getActive()){
            throw new RuntimeException("Cannot hard delete active employee");
        }
        repository.deleteById(id);
    }
    public List<Employee> getByDepartment(String department){
        return repository.findByDepartment(department);
    }
    public List<Employee> getBySalaryRange(BigDecimal min, BigDecimal max){
        return repository.findBySalaryRange(min, max);
    }

    public void validateSalary(String department, BigDecimal salary){
        if("Intern".equalsIgnoreCase(department)){
            if(salary.compareTo(new BigDecimal("15000"))<0){
                throw new RuntimeException("Intern salary must be at least 15000");
            }
        }else{
            if(salary.compareTo(new BigDecimal("30000"))<0){
                throw new RuntimeException("Minimum salary is 30000");
            }
        }
    }

    @Override
    @Transactional
    public ImportResultDto importFromExcel(MultipartFile file) {
        if(file == null || !file.getOriginalFilename().endsWith(".xlsx")){
            throw new InvalidFileFormatException("Only .xlsx files are supported");
        }
        int success = 0;
        int failure = 0;
        List<String>errors = new ArrayList<>();

        try(
                Workbook workbook = new XSSFWorkbook(file.getInputStream())){
            Sheet sheet =workbook.getSheetAt(0);
            for (int i = 1; i < sheet.getLastRowNum() ; i++) {
                Row row = sheet.getRow(i);
                try{
                    EmployeeRequestDto employeeRequestDto = new EmployeeRequestDto();
                    employeeRequestDto.setFirstName(getCellValue(row.getCell(0)));
                    employeeRequestDto.setLastName(getCellValue(row.getCell(1)));
                    employeeRequestDto.setEmail(getCellValue(row.getCell(2)));
                    employeeRequestDto.setDepartment(getCellValue(row.getCell(3)));
                    employeeRequestDto.setActive(Boolean.parseBoolean(getCellValue(row.getCell(4))));
                    employeeRequestDto.setSalary(new BigDecimal(getCellValue(row.getCell(5))));
                    employeeRequestDto.setDateOfJoining(LocalDate.parse(getCellValue(row.getCell(6))));

                    Employee savedEmployee = createEmployee(mapToEntity(employeeRequestDto));
                    success++;
                }catch(Exception ex){
                    failure++;
                    errors.add("Row " + (i + 1) + ": " + ex.getMessage());
                }
            }
        }catch(Exception e){
            throw new ExcelProcessingException("Error processing file");
        }
        return new ImportResultDto(success, failure, errors);
    }

    public String getCellValue(Cell cell){
        if(cell == null){
            return " ";
        }
        return switch(cell.getCellType()){
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());

            default -> "";

        };

    }

    public void exportToExcel(HttpServletResponse response, String department, Boolean active){
        try(Workbook workbook = new XSSFWorkbook()){
            Sheet sheet = workbook.createSheet("Employees");
            List<Employee> employees = repository.findAll();

            // filter
            if(department != null){
                employees = employees.stream()
                        .filter(e->department.equalsIgnoreCase(e.getDepartment()))
                        .toList();
            }
            if(active != null){
                employees = employees.stream()
                        .filter(e->active.equals(e.getActive()))
                        .toList();
            }

            // Header
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "ID", "First Name", "Last Name", "Email", "Department", "Salary", "Date of Joining", "Active", "Created At", "Updated At"
            };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data Row
            int rowIndex = 1;
            for(Employee e : employees){
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(e.getId());
                row.createCell(1).setCellValue(e.getFirstName());
                row.createCell(2).setCellValue(e.getLastName());
                row.createCell(3).setCellValue(e.getEmail());
                row.createCell(4).setCellValue(e.getDepartment());
                row.createCell(5).setCellValue(e.getSalary().doubleValue());
                row.createCell(6).setCellValue(e.getDateOfJoining());
                row.createCell(7).setCellValue(e.getActive());
                row.createCell(8).setCellValue(e.getCreatedAt() != null ? e.getCreatedAt().toString():"");
                row.createCell(9).setCellValue(e.getUpdatedAt() != null ? e.getUpdatedAt().toString():"");

            }

            //Auto Size
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Response Headers
            response.setContentType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            );

            String fileName = "employee_" + System.currentTimeMillis() + ".xlsx";

            response.setHeader(
                    "Content-Disposition",
                    "attachment; filename=" + fileName

            );
            workbook.write(response.getOutputStream());
        }catch (Exception e){
            throw new RuntimeException("Error generating excel file");
        }
    }

    public void exportToPdf(HttpServletResponse response){
        try{
            Document document = new Document();
            response.setContentType("application/pdf");
            String fileName = "employee_report_" + System.currentTimeMillis() + ".pdf";
            response.setHeader(
                    "Content-Disposition",
                    "attachment; filename=" + fileName
            );
            PdfWriter writer =  PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            List<Employee> employees = repository.findAll();

            //Title
            Font titleFont = (Font) FontFactory.getFont(FontFactory.HELVETICA, 16, java.awt.Color.decode(FontFactory.HELVETICA_BOLD));
            Paragraph title = new Paragraph("Employee Report", (org.openpdf.text.Font) titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph("Generated at: " + LocalDateTime.now()));
            document.add(new Paragraph("Total Records: " + employees.size()));
            document.add(new Paragraph(" "));

            //Table
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            addTableHeader(table);
            for (Employee e : employees){
                addRow(table, e);
            }
            document.add(table);
            document.close();

        }catch (Exception e){
            throw new RuntimeException("Error generate=ing PDF");
        }
    }

    private void addTableHeader(PdfPTable table) {

        String[] headers = {
                "ID", "Name", "Email", "Department",
                "Salary", "Date Joined", "Status"
        };

        for (String header : headers) {
            PdfPCell cell = new PdfPCell();
            cell.setPhrase(new Phrase(header));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }

    private void addRow(PdfPTable table, Employee e) {

        table.addCell(String.valueOf(e.getId()));
        table.addCell(e.getFirstName() + " " + e.getLastName());
        table.addCell(e.getEmail());
        table.addCell(e.getDepartment());
        table.addCell(e.getSalary().toString());
        table.addCell(e.getDateOfJoining().toString());
        table.addCell(e.getActive() ? "Active" : "Inactive");
    }

//    @Override
//    public Page<Employee> getEmployees(
//            String department,
//            Boolean active,
//            Pageable pageable) {
//
//        if (department != null && active != null) {
//            return repository.findByDepartmentAndActive(department, active, pageable);
//        }
//
//        if (department != null) {
//            return repository.findByDepartment(department, pageable);
//        }
//
//        if (active != null) {
//            return repository.findByActive(active, pageable);
//        }
//
//        return repository.findAll((org.springframework.data.domain.Pageable) pageable);
//    }

}
