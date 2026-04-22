# Employee Management APP

A CRUD REST API application built with Spring Boot that manages employee records, supports Excel import/export, and generates PDF reports.

## Tech Stack
- Java 21
- Spring Boot 3.x
- Spring Data JPA (Hibernate)
- H2 In-Memory Database
- Apache POI (Excel processing)
- OpenPDF (PDF generation)
- Lombok
- Spring Validation (Jakarta Validation)

## Features
- Full CRUD operations
- Sorting and filtering
- Excel import (.xlsx)
- Excel export
- PDF report generation
- Global exception handling
- Validation with detailed error messages
- Soft delete & hard delete support

## How to Run the Project
  1. Clone the Repository:
        git clone https://github.com/Frank-dev20/employeemanagementsystem
        Cd employee-management-app
  2. Build the Project

        `./mvnw clean install`

Or (if Maven is installed globally):

    `mvn clean install`
3. Run the Application
   `./mvnw spring-boot:run`

4. Access the Application

    Base URL:
    
    http://localhost:8080