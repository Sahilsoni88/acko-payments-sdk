package com.acko.payment.sdk.factory;

import com.acko.payment.sdk.api.PaymentClient;
import com.acko.payment.sdk.auth.AuthService;
import com.acko.payment.sdk.auth.InMemoryTokenStore;
import com.acko.payment.sdk.auth.TokenManager;
import com.acko.payment.sdk.auth.TokenStore;
import com.acko.payment.sdk.common.ExceptionMapper;
import com.acko.payment.sdk.common.FeignClientFactory;
import com.acko.payment.sdk.common.MetricsHook;
import com.acko.payment.sdk.common.RequestExecutor;
import com.acko.payment.sdk.common.RetryExecutor;
import com.acko.payment.sdk.config.SdkConfig;
import com.acko.payment.sdk.payout.PayoutModule;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Objects;

/**
 * Framework-neutral factory for non-Spring consumers and tests.
 */
public final class PaymentClientFactory {

    private PaymentClientFactory() {
    }

    public static PaymentClient create(SdkConfig config) {
        return create(config, new InMemoryTokenStore(), MetricsHook.NOOP, null);
    }

    public static PaymentClient create(SdkConfig config, TokenStore tokenStore) {
        return create(config, tokenStore, MetricsHook.NOOP, null);
    }

    public static PaymentClient create(
            SdkConfig config,
            TokenStore tokenStore,
            MetricsHook metricsHook,
            ObjectMapper objectMapper) {
        Objects.requireNonNull(config, "config");
        Objects.requireNonNull(tokenStore, "tokenStore");

        FeignClientFactory feignClientFactory = objectMapper == null
                ? new FeignClientFactory()
                : new FeignClientFactory(objectMapper);

        AuthService authService = new AuthService(config.getAuth(), feignClientFactory.getObjectMapper());
        TokenManager tokenManager = new TokenManager(authService, tokenStore, config.getTokenCache());
        ExceptionMapper exceptionMapper = new ExceptionMapper();
        RetryExecutor retryExecutor = new RetryExecutor(exceptionMapper);
        RequestExecutor requestExecutor = new RequestExecutor(
                tokenManager, retryExecutor, exceptionMapper, metricsHook);

        return PayoutModule.createPaymentClient(
                requestExecutor,
                config.resolvePayoutRetry(),
                feignClientFactory,
                config.getPayout().getBaseUrl(),
                config.resolvePayoutTimeout());
    }
}
