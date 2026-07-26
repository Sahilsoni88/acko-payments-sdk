package com.acko.payment.sdk.config;

import java.util.Objects;

/**
 * Per-service HTTP settings (base URL + optional timeout/retry overrides).
 */
public final class ServiceSettings {

    private final String baseUrl;
    private final TimeoutSettings timeout;
    private final RetrySettings retry;

    public ServiceSettings(String baseUrl, TimeoutSettings timeout, RetrySettings retry) {
        this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl");
        this.timeout = timeout;
        this.retry = retry;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public TimeoutSettings getTimeout() {
        return timeout;
    }

    public RetrySettings getRetry() {
        return retry;
    }
}
