package com.acko.payment.sdk.config;

import java.time.Duration;
import java.util.Objects;

public final class TokenCacheSettings {

    private final Duration refreshBuffer;
    private final String cacheKey;

    public TokenCacheSettings(Duration refreshBuffer, String cacheKey) {
        this.refreshBuffer = Objects.requireNonNullElse(refreshBuffer, Duration.ofSeconds(30));
        this.cacheKey = cacheKey == null || cacheKey.isBlank()
                ? "acko-payment-sdk:oauth-token"
                : cacheKey;
    }

    public static TokenCacheSettings defaults() {
        return new TokenCacheSettings(Duration.ofSeconds(30), "acko-payment-sdk:oauth-token");
    }

    public Duration getRefreshBuffer() {
        return refreshBuffer;
    }

    public String getCacheKey() {
        return cacheKey;
    }
}
