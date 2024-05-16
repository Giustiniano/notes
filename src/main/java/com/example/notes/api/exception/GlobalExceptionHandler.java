package com.example.notes.api.exception;

import com.fasterxml.jackson.core.JacksonException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(JacksonException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiError> handleJsonDeserializationError(JacksonException e) {
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST.name(), "Note is not valid", e.getMessage());
        return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(apiError);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiError> handleJavaBeansValidationError(MethodArgumentNotValidException e) {
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST.name(), "Note is not valid", e.getMessage());
        return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(apiError);
    }
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<?> handleNoteNotFound(ResourceNotFoundException ex){
        return ResponseEntity.notFound().build();
    }
    @ExceptionHandler(InvalidParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiError> handleInvalidUserParameter(InvalidParameterException ex){
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST.name(), "the note id is not a valid UUIDv4", null);
        return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(apiError);
    }
}
