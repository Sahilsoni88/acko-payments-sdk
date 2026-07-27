package com.acko.payment.sdk.exception;

/**
 * Thrown when the platform returns an unexpected error or retries are exhausted.
 */
public class DownstreamException extends PaymentException {

    private final Integer httpStatus;

    public DownstreamException(String message) {
        this(message, null, null);
    }

    public DownstreamException(String message, Throwable cause) {
        this(message, null, cause);
    }

    public DownstreamException(String message, Integer httpStatus, Throwable cause) {
        super("DOWNSTREAM_ERROR", message, cause);
        this.httpStatus = httpStatus;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }
}
