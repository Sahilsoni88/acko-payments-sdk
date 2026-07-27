package com.acko.payment.sdk.common;

import com.acko.payment.sdk.config.RetrySettings;

import java.util.Objects;

/**
 * Per-call metadata for the request pipeline.
 */
public final class RequestContext {

    private final String operation;
    private final String correlationId;
    private final RetrySettings retrySettings;
    private final boolean retrySafe;

    public RequestContext(String operation, String correlationId, RetrySettings retrySettings, boolean retrySafe) {
        this.operation = Objects.requireNonNull(operation, "operation");
        this.correlationId = correlationId;
        this.retrySettings = Objects.requireNonNull(retrySettings, "retrySettings");
        this.retrySafe = retrySafe;
    }

    public String getOperation() {
        return operation;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public RetrySettings getRetrySettings() {
        return retrySettings;
    }

    /**
     * When false, the executor will not retry even on transient failures
     * (used for money-moving calls without idempotency guarantees).
     */
    public boolean isRetrySafe() {
        return retrySafe;
    }
}
