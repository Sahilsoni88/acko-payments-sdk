package com.acko.payment.sdk.config;

import java.time.Duration;
import java.util.Objects;

/**
 * Connect/read timeout settings for HTTP clients.
 */
public final class TimeoutSettings {

    private final Duration connect;
    private final Duration read;

    public TimeoutSettings(Duration connect, Duration read) {
        this.connect = Objects.requireNonNullElse(connect, Duration.ofSeconds(2));
        this.read = Objects.requireNonNullElse(read, Duration.ofSeconds(5));
    }

    public static TimeoutSettings defaults() {
        return new TimeoutSettings(Duration.ofSeconds(2), Duration.ofSeconds(5));
    }

    public Duration getConnect() {
        return connect;
    }

    public Duration getRead() {
        return read;
    }
}
