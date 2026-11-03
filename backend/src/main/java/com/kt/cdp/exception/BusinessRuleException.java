package com.kt.cdp.exception;

/**
 * Thrown when a business rule / invalid state transition is violated.
 * Maps to HTTP 422 (Unprocessable Entity).
 */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
