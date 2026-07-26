package com.acko.payment.sdk.auth;

import java.util.Optional;

/**
 * Pluggable token cache. v0 ships with {@link InMemoryTokenStore}.
 */
public interface TokenStore {

    Optional<OAuthToken> get(String key);

    void store(String key, OAuthToken token);

    void invalidate(String key);
}
