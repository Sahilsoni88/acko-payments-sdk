package com.acko.payment.sdk.exception;

/**
 * Thrown when a platform call times out. For mutating payout calls, recover via {@code verify}.
 */
public class PaymentTimeoutException extends PaymentException {

    public PaymentTimeoutException(String message) {
        super("TIMEOUT", message);
    }

    public PaymentTimeoutException(String message, Throwable cause) {
        super("TIMEOUT", message, cause);
    }
}
