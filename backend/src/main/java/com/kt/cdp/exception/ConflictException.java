package com.kt.cdp.exception;

/** Thrown on uniqueness / duplicate conflicts. Maps to HTTP 409. */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
