package com.acko.payment.sdk.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Spring Boot configuration properties under {@code payment.*}.
 */
@ConfigurationProperties(prefix = "payment")
public class PaymentProperties {

    private Defaults defaults = new Defaults();
    private Auth auth = new Auth();
    private Service payout = new Service();
    private TokenCache tokenCache = new TokenCache();

    public Defaults getDefaults() {
        return defaults;
    }

    public void setDefaults(Defaults defaults) {
        this.defaults = defaults;
    }

    public Auth getAuth() {
        return auth;
    }

    public void setAuth(Auth auth) {
        this.auth = auth;
    }

    public Service getPayout() {
        return payout;
    }

    public void setPayout(Service payout) {
        this.payout = payout;
    }

    public TokenCache getTokenCache() {
        return tokenCache;
    }

    public void setTokenCache(TokenCache tokenCache) {
        this.tokenCache = tokenCache;
    }

    public static class Defaults {
        private Timeout timeout = new Timeout();
        private Retry retry = new Retry();

        public Timeout getTimeout() {
            return timeout;
        }

        public void setTimeout(Timeout timeout) {
            this.timeout = timeout;
        }

        public Retry getRetry() {
            return retry;
        }

        public void setRetry(Retry retry) {
            this.retry = retry;
        }
    }

    public static class Auth {
        private String tokenUrl;
        private String clientId;
        private String clientSecret;
        private String scope;

        public String getTokenUrl() {
            return tokenUrl;
        }

        public void setTokenUrl(String tokenUrl) {
            this.tokenUrl = tokenUrl;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }

        public String getScope() {
            return scope;
        }

        public void setScope(String scope) {
            this.scope = scope;
        }
    }

    public static class Service {
        private String baseUrl;
        private Timeout timeout;
        private Retry retry;
        private String cookieHeader;

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public Timeout getTimeout() {
            return timeout;
        }

        public void setTimeout(Timeout timeout) {
            this.timeout = timeout;
        }

        public Retry getRetry() {
            return retry;
        }

        public void setRetry(Retry retry) {
            this.retry = retry;
        }

        public String getCookieHeader() {
            return cookieHeader;
        }

        public void setCookieHeader(String cookieHeader) {
            this.cookieHeader = cookieHeader;
        }
    }

    public static class Timeout {
        private Duration connect = Duration.ofSeconds(2);
        private Duration read = Duration.ofSeconds(5);

        public Duration getConnect() {
            return connect;
        }

        public void setConnect(Duration connect) {
            this.connect = connect;
        }

        public Duration getRead() {
            return read;
        }

        public void setRead(Duration read) {
            this.read = read;
        }
    }

    public static class Retry {
        private boolean enabled = true;
        private int maxAttempts = 3;
        private Duration backoff = Duration.ofMillis(500);

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public Duration getBackoff() {
            return backoff;
        }

        public void setBackoff(Duration backoff) {
            this.backoff = backoff;
        }
    }

    public static class TokenCache {
        private Duration refreshBuffer = Duration.ofSeconds(30);
        private String cacheKey = "acko-payment-sdk:oauth-token";

        public Duration getRefreshBuffer() {
            return refreshBuffer;
        }

        public void setRefreshBuffer(Duration refreshBuffer) {
            this.refreshBuffer = refreshBuffer;
        }

        public String getCacheKey() {
            return cacheKey;
        }

        public void setCacheKey(String cacheKey) {
            this.cacheKey = cacheKey;
        }
    }
}
