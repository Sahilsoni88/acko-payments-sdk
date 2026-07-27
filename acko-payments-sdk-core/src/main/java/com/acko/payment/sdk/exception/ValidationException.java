package com.acko.payment.sdk.exception;

/**
 * Thrown for client-side or platform 4xx validation failures. Do not blind-retry.
 */
public class ValidationException extends PaymentException {

    private final int httpStatus;

    public ValidationException(String message) {
        this(message, 400);
    }

    public ValidationException(String message, int httpStatus) {
        super("VALIDATION_ERROR", message);
        this.httpStatus = httpStatus;
    }

    public ValidationException(String message, int httpStatus, Throwable cause) {
        super("VALIDATION_ERROR", message, cause);
        this.httpStatus = httpStatus;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
