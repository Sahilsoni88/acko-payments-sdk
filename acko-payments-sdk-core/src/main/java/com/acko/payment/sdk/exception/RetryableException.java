package com.acko.payment.sdk.exception;

/**
 * Internal signal for transient failures (5xx, network, timeout).
 * After retries are exhausted this is mapped to {@link DownstreamException}.
 */
public class RetryableException extends PaymentException {

    public RetryableException(String message) {
        super("RETRYABLE_ERROR", message);
    }

    public RetryableException(String message, Throwable cause) {
        super("RETRYABLE_ERROR", message, cause);
    }
}
