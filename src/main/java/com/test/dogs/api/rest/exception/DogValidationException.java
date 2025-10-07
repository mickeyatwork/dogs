package com.test.dogs.api.rest.exception;

public class DogValidationException extends RuntimeException {
    public DogValidationException(String message) {
        super(message);
    }

    public DogValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
