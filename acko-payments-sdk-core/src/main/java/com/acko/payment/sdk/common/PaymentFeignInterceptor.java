package com.acko.payment.sdk.common;

import feign.RequestInterceptor;
import feign.RequestTemplate;

/**
 * Adds request-scoped auth and correlation headers. Package-private usage via Feign builder.
 */
final class PaymentFeignInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        String authorization = AccessTokenHolder.get();
        if (authorization != null && !authorization.isBlank()) {
            template.header("Authorization", authorization);
        }
        String cookie = CookieHolder.get();
        if (cookie != null && !cookie.isBlank()) {
            template.header("Cookie", cookie);
        }
        String correlationId = com.acko.payment.sdk.util.CorrelationIdHolder.get().orElse(null);
        if (correlationId != null) {
            template.header("X-Correlation-Id", correlationId);
        }
    }
}
