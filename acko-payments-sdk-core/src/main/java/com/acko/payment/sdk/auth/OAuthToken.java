package com.acko.payment.sdk.auth;

import java.time.Instant;
import java.util.Objects;

/**
 * Cached OAuth2 access token with expiry.
 */
public final class OAuthToken {

    private final String accessToken;
    private final String tokenType;
    private final Instant expiresAt;

    public OAuthToken(String accessToken, String tokenType, Instant expiresAt) {
        this.accessToken = Objects.requireNonNull(accessToken, "accessToken");
        this.tokenType = tokenType == null || tokenType.isBlank() ? "Bearer" : tokenType;
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt");
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isExpired(Instant now, java.time.Duration refreshBuffer) {
        Instant effectiveExpiry = expiresAt.minus(refreshBuffer);
        return !now.isBefore(effectiveExpiry);
    }

    public String authorizationHeader() {
        return tokenType + " " + accessToken;
    }
}
