package com.fintech.Exception;


public class InternalServerException extends AppException {
    public InternalServerException(String message) {
        super(message, 500);
    }
}