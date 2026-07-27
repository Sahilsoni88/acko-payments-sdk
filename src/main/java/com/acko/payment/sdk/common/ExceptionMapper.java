package com.acko.payment.sdk.common;

import com.acko.payment.sdk.exception.AuthenticationException;
import com.acko.payment.sdk.exception.DownstreamException;
import com.acko.payment.sdk.exception.PaymentTimeoutException;
import com.acko.payment.sdk.exception.RetryableException;
import com.acko.payment.sdk.exception.ValidationException;
import feign.FeignException;
import feign.codec.DecodeException;

import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;

/**
 * Maps Feign / HTTP failures to typed SDK exceptions.
 */
public final class ExceptionMapper {

    public RuntimeException map(Throwable throwable) {
        if (throwable instanceof feign.RetryableException feignRetryable) {
            if (isTimeout(feignRetryable)) {
                return new PaymentTimeoutException("Payment platform call timed out", feignRetryable);
            }
            return new RetryableException("Transient Feign failure: " + feignRetryable.getMessage(), feignRetryable);
        }
        if (throwable instanceof FeignException feignException) {
            int status = feignException.status();
            if (status == 401 || status == 403) {
                return new AuthenticationException(
                        "Authentication failed against payment platform, status=" + status, feignException);
            }
            if (status >= 400 && status < 500) {
                return new ValidationException(
                        "Validation or client error from payment platform, status=" + status
                                + ": " + safeBody(feignException),
                        status,
                        feignException);
            }
            if (status >= 500) {
                return new RetryableException(
                        "Payment platform server error, status=" + status, feignException);
            }
            return new DownstreamException(
                    "Unexpected payment platform response, status=" + status, status, feignException);
        }
        if (throwable instanceof DecodeException decodeException) {
            return new DownstreamException("Failed to decode payment platform response", decodeException);
        }
        if (isTimeout(throwable)) {
            return new PaymentTimeoutException("Payment platform call timed out", throwable);
        }
        if (throwable instanceof RuntimeException runtimeException) {
            return runtimeException;
        }
        return new DownstreamException("Unexpected error calling payment platform", throwable);
    }

    private static boolean isTimeout(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof SocketTimeoutException
                    || current instanceof TimeoutException
                    || hasTimeoutMessage(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private static boolean hasTimeoutMessage(Throwable throwable) {
        if (throwable.getMessage() == null) {
            return false;
        }
        String message = throwable.getMessage().toLowerCase();
        return message.contains("timeout") || message.contains("timed out");
    }

    private static String safeBody(FeignException exception) {
        try {
            byte[] body = exception.content();
            if (body == null || body.length == 0) {
                return "";
            }
            String text = new String(body);
            return text.length() > 500 ? text.substring(0, 500) + "..." : text;
        } catch (Exception ignored) {
            return "";
        }
    }
}
