package com.acko.payment.sdk.auth;

import com.acko.payment.sdk.config.AuthSettings;
import com.acko.payment.sdk.exception.AuthenticationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Fetches OAuth2 tokens via client-credentials grant using JDK {@link HttpClient}.
 */
class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AuthSettings settings;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public AuthService(AuthSettings settings, ObjectMapper objectMapper) {
        this(settings, objectMapper, HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build());
    }

    AuthService(AuthSettings settings, ObjectMapper objectMapper, HttpClient httpClient) {
        this.settings = Objects.requireNonNull(settings, "settings");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
    }

    public OAuthToken fetchToken() {
        try {
            String form = "grant_type=client_credentials"
                    + "&client_id=" + encode(settings.getClientId())
                    + "&client_secret=" + encode(settings.getClientSecret());
            if (settings.getScope() != null && !settings.getScope().isBlank()) {
                form += "&scope=" + encode(settings.getScope());
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(settings.getTokenUrl()))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(form))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.error("OAuth token fetch failed status={}", response.statusCode());
                throw new AuthenticationException(
                        "Failed to fetch OAuth token, status=" + response.statusCode());
            }

            JsonNode body = objectMapper.readTree(response.body());
            String accessToken = text(body, "access_token");
            if (accessToken == null || accessToken.isBlank()) {
                throw new AuthenticationException("OAuth response missing access_token");
            }
            String tokenType = text(body, "token_type");
            long expiresIn = body.path("expires_in").asLong(3600L);
            Instant expiresAt = Instant.now().plusSeconds(Math.max(expiresIn, 60L));

            log.debug("OAuth token fetched expiresInSeconds={}", expiresIn);
            return new OAuthToken(accessToken, tokenType, expiresAt);
        } catch (AuthenticationException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AuthenticationException("OAuth token fetch interrupted", e);
        } catch (Exception e) {
            throw new AuthenticationException("OAuth token fetch failed", e);
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }
}
