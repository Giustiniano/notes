package com.example.notes.api.exception;

public class InvalidParameterException extends IllegalArgumentException{
    public InvalidParameterException(String message){
        super(message);
    }
}
