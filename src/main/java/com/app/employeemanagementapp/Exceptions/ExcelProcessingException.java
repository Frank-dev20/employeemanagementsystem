package com.app.employeemanagementapp.Exceptions;

public class ExcelProcessingException extends RuntimeException{
    public ExcelProcessingException(String message){
        super(message);
    }
}
