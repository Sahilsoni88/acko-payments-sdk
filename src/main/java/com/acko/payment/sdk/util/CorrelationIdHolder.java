package com.acko.payment.sdk.util;

import java.util.Optional;
import java.util.UUID;

/**
 * Thread-local correlation id for structured logging across a single SDK call.
 */
public final class CorrelationIdHolder {

    private static final ThreadLocal<String> HOLDER = new ThreadLocal<>();

    private CorrelationIdHolder() {
    }

    public static void set(String correlationId) {
        HOLDER.set(correlationId);
    }

    public static String getOrCreate() {
        String existing = HOLDER.get();
        if (existing == null || existing.isBlank()) {
            existing = UUID.randomUUID().toString();
            HOLDER.set(existing);
        }
        return existing;
    }

    public static Optional<String> get() {
        return Optional.ofNullable(HOLDER.get());
    }

    public static void clear() {
        HOLDER.remove();
    }
}
