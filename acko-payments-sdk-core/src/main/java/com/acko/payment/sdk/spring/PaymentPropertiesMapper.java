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

        AuthSettings authSettings = toAuthSettings(properties.getAuth());

        PaymentProperties.Service payout = properties.getPayout();
        ServiceSettings payoutSettings = new ServiceSettings(
                payout.getBaseUrl(),
                payout.getTimeout() == null ? null : toTimeout(payout.getTimeout()),
                payout.getRetry() == null ? null : toRetry(payout.getRetry()),
                payout.getCookieHeader());

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

    private static AuthSettings toAuthSettings(PaymentProperties.Auth auth) {
        if (auth == null
                || isBlank(auth.getTokenUrl())
                || isBlank(auth.getClientId())
                || isBlank(auth.getClientSecret())) {
            return null;
        }
        return new AuthSettings(
                auth.getTokenUrl(),
                auth.getClientId(),
                auth.getClientSecret(),
                auth.getScope());
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
