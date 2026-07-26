package com.acko.payment.sdk.common;

import feign.RequestInterceptor;
import feign.RequestTemplate;

/**
 * Adds Authorization from {@link AccessTokenHolder}. Package-private usage via Feign builder.
 */
public final class PaymentFeignInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        String authorization = AccessTokenHolder.get();
        if (authorization != null && !authorization.isBlank()) {
            template.header("Authorization", authorization);
        }
        String correlationId = com.acko.payment.sdk.util.CorrelationIdHolder.get().orElse(null);
        if (correlationId != null) {
            template.header("X-Correlation-Id", correlationId);
        }
    }
}
