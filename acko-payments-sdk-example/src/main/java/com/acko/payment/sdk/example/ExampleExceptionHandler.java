package com.acko.payment.sdk.example;

import com.acko.payment.sdk.exception.AuthenticationException;
import com.acko.payment.sdk.exception.DownstreamException;
import com.acko.payment.sdk.exception.PaymentException;
import com.acko.payment.sdk.exception.PaymentTimeoutException;
import com.acko.payment.sdk.exception.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class ExampleExceptionHandler {

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ErrorResponse> handlePaymentException(PaymentException exception) {
        HttpStatus status = statusFor(exception);
        return ResponseEntity.status(status)
                .body(new ErrorResponse(exception.getErrorCode(), exception.getMessage(), Instant.now()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_ERROR", exception.getMessage(), Instant.now()));
    }

    private HttpStatus statusFor(PaymentException exception) {
        if (exception instanceof AuthenticationException) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (exception instanceof ValidationException) {
            return HttpStatus.BAD_REQUEST;
        }
        if (exception instanceof PaymentTimeoutException) {
            return HttpStatus.GATEWAY_TIMEOUT;
        }
        if (exception instanceof DownstreamException) {
            return HttpStatus.BAD_GATEWAY;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    public record ErrorResponse(String errorCode, String message, Instant timestamp) {
    }
}
