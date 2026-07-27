package com.acko.payment.sdk.auth;

import com.acko.payment.sdk.config.AuthSettings;
import com.acko.payment.sdk.config.TokenCacheSettings;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread-safe token lifecycle with refresh-before-expiry and double-checked locking.
 */
public class TokenManager {

    private static final Logger log = LoggerFactory.getLogger(TokenManager.class);

    private final AuthService authService;
    private final TokenStore tokenStore;
    private final TokenCacheSettings cacheSettings;
    private final ReentrantLock lock = new ReentrantLock();

    public TokenManager(
            AuthSettings authSettings,
            ObjectMapper objectMapper,
            TokenStore tokenStore,
            TokenCacheSettings cacheSettings) {
        this(new AuthService(authSettings, objectMapper), tokenStore, cacheSettings);
    }

    TokenManager(AuthService authService, TokenStore tokenStore, TokenCacheSettings cacheSettings) {
        this.authService = Objects.requireNonNull(authService, "authService");
        this.tokenStore = Objects.requireNonNull(tokenStore, "tokenStore");
        this.cacheSettings = Objects.requireNonNull(cacheSettings, "cacheSettings");
    }

    public OAuthToken getValidToken() {
        Instant now = Instant.now();
        String key = cacheSettings.getCacheKey();

        OAuthToken cached = tokenStore.get(key).orElse(null);
        if (cached != null && !cached.isExpired(now, cacheSettings.getRefreshBuffer())) {
            return cached;
        }

        lock.lock();
        try {
            cached = tokenStore.get(key).orElse(null);
            if (cached != null && !cached.isExpired(Instant.now(), cacheSettings.getRefreshBuffer())) {
                return cached;
            }
            log.debug("Refreshing OAuth token cacheKey={}", key);
            OAuthToken fresh = authService.fetchToken();
            tokenStore.store(key, fresh);
            return fresh;
        } finally {
            lock.unlock();
        }
    }

    public void invalidate() {
        tokenStore.invalidate(cacheSettings.getCacheKey());
    }
}
