package com.acko.payment.sdk.exception;

/**
 * Thrown when SDK authentication against the platform fails.
 */
public class AuthenticationException extends PaymentException {

    public AuthenticationException(String message) {
        super("AUTHENTICATION_ERROR", message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super("AUTHENTICATION_ERROR", message, cause);
    }
}
