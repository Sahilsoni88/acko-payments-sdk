package com.acko.payment.sdk.spring;

import com.acko.payment.sdk.config.AuthSettings;
import com.acko.payment.sdk.config.RetrySettings;
import com.acko.payment.sdk.config.SdkConfig;
import com.acko.payment.sdk.config.ServiceSettings;
import com.acko.payment.sdk.config.TimeoutSettings;
import com.acko.payment.sdk.config.TokenCacheSettings;

final class PaymentPropertiesMapper {

    private PaymentPropertiesMapper() {
    }

    static SdkConfig toSdkConfig(PaymentProperties properties) {
        TimeoutSettings defaultTimeout = toTimeout(properties.getDefaults().getTimeout());
        RetrySettings defaultRetry = toRetry(properties.getDefaults().getRetry());

        PaymentProperties.Auth auth = properties.getAuth();
        AuthSettings authSettings = new AuthSettings(
                auth.getTokenUrl(),
                auth.getClientId(),
                auth.getClientSecret(),
                auth.getScope());

        PaymentProperties.Service payout = properties.getPayout();
        ServiceSettings payoutSettings = new ServiceSettings(
                payout.getBaseUrl(),
                payout.getTimeout() == null ? null : toTimeout(payout.getTimeout()),
                payout.getRetry() == null ? null : toRetry(payout.getRetry()));

        TokenCacheSettings tokenCache = new TokenCacheSettings(
                properties.getTokenCache().getRefreshBuffer(),
                properties.getTokenCache().getCacheKey());

        return SdkConfig.builder()
                .auth(authSettings)
                .defaultTimeout(defaultTimeout)
                .defaultRetry(defaultRetry)
                .tokenCache(tokenCache)
                .payout(payoutSettings)
                .build();
    }

    private static TimeoutSettings toTimeout(PaymentProperties.Timeout timeout) {
        return new TimeoutSettings(timeout.getConnect(), timeout.getRead());
    }

    private static RetrySettings toRetry(PaymentProperties.Retry retry) {
        return new RetrySettings(retry.isEnabled(), retry.getMaxAttempts(), retry.getBackoff());
    }
}
