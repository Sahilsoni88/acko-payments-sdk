package com.acko.payment.sdk.api;

/**
 * Single public entry point for the Payment SDK.
 *
 * <p>v0 exposes {@link #payout()} only. Payin (including refunds) will be added in a later release.
 */
public interface PaymentClient {

    /**
     * Access payout operations (disbursal, validation, verify).
     */
    PayoutOperations payout();
}
