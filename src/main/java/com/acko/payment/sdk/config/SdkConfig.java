package com.acko.payment.sdk.config;

import java.util.Objects;

/**
 * Framework-neutral SDK configuration used by {@link com.acko.payment.sdk.factory.PaymentClientFactory}.
 */
public final class SdkConfig {

    private final AuthSettings auth;
    private final TimeoutSettings defaultTimeout;
    private final RetrySettings defaultRetry;
    private final TokenCacheSettings tokenCache;
    private final ServiceSettings payout;

    private SdkConfig(Builder builder) {
        this.auth = Objects.requireNonNull(builder.auth, "auth");
        this.defaultTimeout = Objects.requireNonNullElse(builder.defaultTimeout, TimeoutSettings.defaults());
        this.defaultRetry = Objects.requireNonNullElse(builder.defaultRetry, RetrySettings.defaults());
        this.tokenCache = Objects.requireNonNullElse(builder.tokenCache, TokenCacheSettings.defaults());
        this.payout = Objects.requireNonNull(builder.payout, "payout");
    }

    public static Builder builder() {
        return new Builder();
    }

    public AuthSettings getAuth() {
        return auth;
    }

    public TimeoutSettings getDefaultTimeout() {
        return defaultTimeout;
    }

    public RetrySettings getDefaultRetry() {
        return defaultRetry;
    }

    public TokenCacheSettings getTokenCache() {
        return tokenCache;
    }

    public ServiceSettings getPayout() {
        return payout;
    }

    public TimeoutSettings resolvePayoutTimeout() {
        return payout.getTimeout() != null ? payout.getTimeout() : defaultTimeout;
    }

    public RetrySettings resolvePayoutRetry() {
        return payout.getRetry() != null ? payout.getRetry() : defaultRetry;
    }

    public static final class Builder {
        private AuthSettings auth;
        private TimeoutSettings defaultTimeout;
        private RetrySettings defaultRetry;
        private TokenCacheSettings tokenCache;
        private ServiceSettings payout;

        public Builder auth(AuthSettings auth) {
            this.auth = auth;
            return this;
        }

        public Builder defaultTimeout(TimeoutSettings defaultTimeout) {
            this.defaultTimeout = defaultTimeout;
            return this;
        }

        public Builder defaultRetry(RetrySettings defaultRetry) {
            this.defaultRetry = defaultRetry;
            return this;
        }

        public Builder tokenCache(TokenCacheSettings tokenCache) {
            this.tokenCache = tokenCache;
            return this;
        }

        public Builder payout(ServiceSettings payout) {
            this.payout = payout;
            return this;
        }

        public SdkConfig build() {
            return new SdkConfig(this);
        }
    }
}
