package com.dormfix.user;

/** Raised when a login email or password is invalid. */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
