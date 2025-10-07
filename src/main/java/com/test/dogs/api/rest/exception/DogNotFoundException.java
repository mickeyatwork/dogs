package com.test.dogs.api.rest.exception;

public class DogNotFoundException extends RuntimeException {
    public DogNotFoundException(String message) {
        super(message);
    }

    public DogNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
