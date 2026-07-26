package com.acko.payment.sdk.payout;

import com.acko.payment.sdk.api.PaymentClient;
import com.acko.payment.sdk.common.FeignClientFactory;
import com.acko.payment.sdk.common.RequestExecutor;
import com.acko.payment.sdk.config.RetrySettings;
import com.acko.payment.sdk.config.TimeoutSettings;

/**
 * Assembles payout wiring while keeping {@link PayoutFeignClient} package-private.
 */
public final class PayoutModule {

    private PayoutModule() {
    }

    public static PaymentClient createPaymentClient(
            RequestExecutor requestExecutor,
            RetrySettings retrySettings,
            FeignClientFactory feignClientFactory,
            String payoutBaseUrl,
            TimeoutSettings timeoutSettings) {
        PayoutFeignClient feignClient = feignClientFactory.create(
                PayoutFeignClient.class, payoutBaseUrl, timeoutSettings);
        DefaultPayoutService payoutService = new DefaultPayoutService(
                feignClient, requestExecutor, retrySettings);
        return new DefaultPaymentClient(payoutService);
    }
}
