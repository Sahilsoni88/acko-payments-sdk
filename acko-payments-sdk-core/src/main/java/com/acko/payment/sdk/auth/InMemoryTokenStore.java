package com.acko.payment.sdk.auth;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Process-local token cache suitable for single-instance consumers.
 */
public final class InMemoryTokenStore implements TokenStore {

    private final ConcurrentHashMap<String, OAuthToken> tokens = new ConcurrentHashMap<>();

    @Override
    public Optional<OAuthToken> get(String key) {
        return Optional.ofNullable(tokens.get(key));
    }

    @Override
    public void store(String key, OAuthToken token) {
        tokens.put(key, token);
    }

    @Override
    public void invalidate(String key) {
        tokens.remove(key);
    }
}
