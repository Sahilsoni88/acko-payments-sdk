package com.acko.payment.sdk.payout;

import com.acko.payment.sdk.api.PaymentClient;
import com.acko.payment.sdk.api.PayoutOperations;

import java.util.Objects;

/**
 * Package-private {@link PaymentClient} implementation for v0 (payout only).
 */
public final class DefaultPaymentClient implements PaymentClient {

    private final PayoutOperations payoutOperations;

    public DefaultPaymentClient(PayoutOperations payoutOperations) {
        this.payoutOperations = Objects.requireNonNull(payoutOperations, "payoutOperations");
    }

    @Override
    public PayoutOperations payout() {
        return payoutOperations;
    }
}
