package com.acko.payment.sdk.api;

import com.acko.payment.sdk.payout.GeneratePayoutRequestIdResponse;
import com.acko.payment.sdk.payout.InitiatePayoutRequest;
import com.acko.payment.sdk.payout.InitiatePayoutResponse;
import com.acko.payment.sdk.payout.UpdatePayoutDetailsRequest;
import com.acko.payment.sdk.payout.UpdatePayoutDetailsResponse;
import com.acko.payment.sdk.payout.ValidateAccountDetailsRequest;
import com.acko.payment.sdk.payout.ValidateAccountDetailsResponse;
import com.acko.payment.sdk.payout.VerifyIfscResponse;
import com.acko.payment.sdk.payout.VerifyPayoutResponse;

/**
 * Payout operations mirroring {@code PayoutServiceClient}.
 *
 * <p>On timeout for mutating calls ({@link #initiate}, {@link #updatePayoutDetails}),
 * call {@link #verify(String)} — do not blind-retry initiate.
 */
public interface PayoutOperations {

    /**
     * Allocates a platform payout request id.
     * Maps to {@code POST /api/generate_payout_request_id}.
     */
    GeneratePayoutRequestIdResponse generatePayoutRequestId();

    /**
     * Validates an IFSC code.
     * Maps to {@code GET /api/ifsc-verify}.
     */
    VerifyIfscResponse verifyIfsc(String ifsc);

    /**
     * Pre-flight validation of beneficiary account details.
     * Maps to {@code POST /api/validate/account_details}.
     */
    ValidateAccountDetailsResponse validateAccountDetails(ValidateAccountDetailsRequest request);

    /**
     * Initiates a payout. Persist {@code payout_request_id} before/at this call.
     * Maps to {@code POST /api/v2/initiate_payout}.
     */
    InitiatePayoutResponse initiate(InitiatePayoutRequest request);

    /**
     * Updates payout details when the platform allows.
     * Maps to {@code POST /api/v2/update_payout_details}.
     */
    UpdatePayoutDetailsResponse updatePayoutDetails(UpdatePayoutDetailsRequest request);

    /**
     * Sync status check — preferred recovery after timeout / uncertainty.
     * Maps to {@code GET /api/{payout_request_id}/verify}.
     */
    VerifyPayoutResponse verify(String payoutRequestId);
}
