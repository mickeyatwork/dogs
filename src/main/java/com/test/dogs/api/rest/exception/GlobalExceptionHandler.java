package com.test.dogs.api.rest.exception;

import com.test.dogs.api.rest.model.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DogNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDogNotFoundException(
            DogNotFoundException ex, WebRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
            ex.getMessage(),
            HttpStatus.NOT_FOUND.value(),
            "Not Found",
            request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DogValidationException.class)
    public ResponseEntity<ErrorResponse> handleDogValidationException(
            DogValidationException ex, WebRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
            ex.getMessage(),
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DogServiceException.class)
    public ResponseEntity<ErrorResponse> handleDogServiceException(
            DogServiceException ex, WebRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
            ex.getMessage(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, WebRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
            "An unexpected error occurred: " + ex.getMessage(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
