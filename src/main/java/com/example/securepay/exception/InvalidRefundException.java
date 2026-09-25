package com.example.securepay.exception;

public class InvalidRefundException extends RuntimeException {

    public InvalidRefundException(String message) {
        super(message);
    }
}