package com.app.employeemanagementapp.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ImportResultDto {
    private int successCount;
    private int failureCount;
    private List<String> errors;
}
