package com.app.employeemanagementapp;

import jakarta.persistence.Entity;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

@SpringBootApplication
public class EmployeeManagementAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmployeeManagementAppApplication.class, args);
    }

}
