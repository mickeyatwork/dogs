package com.test.dogs.api.rest.exception;

public class DogServiceException extends RuntimeException {
    public DogServiceException(String message) {
        super(message);
    }

    public DogServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
