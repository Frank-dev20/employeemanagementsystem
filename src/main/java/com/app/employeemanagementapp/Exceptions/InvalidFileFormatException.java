package com.app.employeemanagementapp.Exceptions;

public class InvalidFileFormatException extends RuntimeException{
    public InvalidFileFormatException(String message){
        super(message);
    }
}
