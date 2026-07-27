package com.acko.payment.sdk.payout;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

/**
 * Package-private Feign client for the Payout platform.
 */
interface PayoutFeignClient {

    @RequestLine("POST /api/generate_payout_request_id")
    @Headers("Content-Type: application/json")
    GeneratePayoutRequestIdResponse generatePayoutRequestId();

    @RequestLine("GET /api/ifsc-verify?ifsc={ifsc}")
    VerifyIfscResponse verifyIfsc(@Param("ifsc") String ifsc);

    @RequestLine("POST /api/validate/account_details")
    @Headers("Content-Type: application/json")
    ValidateAccountDetailsResponse validateAccountDetails(ValidateAccountDetailsRequest request);

    @RequestLine("POST /api/v2/initiate_payout")
    @Headers("Content-Type: application/json")
    InitiatePayoutResponse initiate(InitiatePayoutRequest request);

    @RequestLine("POST /api/initiate_payout/")
    @Headers("Content-Type: application/json")
    InitiatePayoutResponse initiateV1(InitiatePayoutRequest request);

    @RequestLine("POST /api/v2/update_payout_details")
    @Headers("Content-Type: application/json")
    UpdatePayoutDetailsResponse updatePayoutDetails(UpdatePayoutDetailsRequest request);

    @RequestLine("GET /api/{payoutRequestId}/verify")
    VerifyPayoutResponse verify(@Param("payoutRequestId") String payoutRequestId);
}
