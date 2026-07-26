package com.acko.payment.sdk.config;

import java.util.Objects;

/**
 * OAuth2 client-credentials settings.
 */
public final class AuthSettings {

    private final String tokenUrl;
    private final String clientId;
    private final String clientSecret;
    private final String scope;

    public AuthSettings(String tokenUrl, String clientId, String clientSecret, String scope) {
        this.tokenUrl = Objects.requireNonNull(tokenUrl, "tokenUrl");
        this.clientId = Objects.requireNonNull(clientId, "clientId");
        this.clientSecret = Objects.requireNonNull(clientSecret, "clientSecret");
        this.scope = scope;
    }

    public String getTokenUrl() {
        return tokenUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getScope() {
        return scope;
    }
}
