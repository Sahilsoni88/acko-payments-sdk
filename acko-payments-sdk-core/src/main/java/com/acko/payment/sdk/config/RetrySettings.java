package com.acko.payment.sdk.config;

import java.time.Duration;
import java.util.Objects;

/**
 * Retry policy for transient platform failures.
 */
public final class RetrySettings {

    private final boolean enabled;
    private final int maxAttempts;
    private final Duration backoff;

    public RetrySettings(boolean enabled, int maxAttempts, Duration backoff) {
        this.enabled = enabled;
        this.maxAttempts = maxAttempts <= 0 ? 1 : maxAttempts;
        this.backoff = Objects.requireNonNullElse(backoff, Duration.ofMillis(500));
    }

    public static RetrySettings defaults() {
        return new RetrySettings(true, 3, Duration.ofMillis(500));
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public Duration getBackoff() {
        return backoff;
    }
}
