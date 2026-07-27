package com.acko.payment.sdk.factory;

import com.acko.payment.sdk.api.PaymentClient;
import com.acko.payment.sdk.auth.InMemoryTokenStore;
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

        ExceptionMapper exceptionMapper = new ExceptionMapper();
        RetryExecutor retryExecutor = new RetryExecutor(exceptionMapper);
        RequestExecutor payoutRequestExecutor = RequestExecutor.cookieAuthenticated(
                config.getPayout().getCookieHeader(), retryExecutor, exceptionMapper, metricsHook);

        return PayoutModule.createPaymentClient(
                payoutRequestExecutor,
                config.resolvePayoutRetry(),
                feignClientFactory,
                config.getPayout().getBaseUrl(),
                config.resolvePayoutTimeout());
    }
}
