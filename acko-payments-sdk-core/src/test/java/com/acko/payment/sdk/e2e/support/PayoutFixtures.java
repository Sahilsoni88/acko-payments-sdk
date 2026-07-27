package com.acko.payment.sdk.e2e.support;

import com.acko.payment.sdk.payout.InitiatePayoutRequest;
import com.acko.payment.sdk.payout.PaymentInstrument;
import com.acko.payment.sdk.payout.UpdatePayoutDetailsRequest;
import com.acko.payment.sdk.payout.ValidateAccountDetailsRequest;

import java.math.BigDecimal;

public final class PayoutFixtures {

    private PayoutFixtures() {
    }

    public static ValidateAccountDetailsRequest validateAccountRequest() {
        return ValidateAccountDetailsRequest.builder()
                .accountNumber("1234567890")
                .ifscCode("HDFC0001234")
                .accountHolderName("Ada Lovelace")
                .accountType("bank")
                .entityId("cust-1")
                .build();
    }

    public static InitiatePayoutRequest initiateRequest(String payoutRequestId) {
        return InitiatePayoutRequest.builder()
                .okind("jarvis")
                .oid("claim-1")
                .paymentType("claim")
                .amount(new BigDecimal("250.50"))
                .requestedById("user-1")
                .entityType("customer")
                .entityId(101L)
                .callbackUrl("https://callback.example/payout")
                .paymentMode("bank")
                .payoutRequestId(payoutRequestId)
                .uniqueId("idem-1")
                .paymentInstrument(PaymentInstrument.builder()
                        .accountNumber("1234567890")
                        .ifscCode("HDFC0001234")
                        .accountHolderName("Ada Lovelace")
                        .accountType("savings")
                        .beneficiaryId("ben-1")
                        .build())
                .build();
    }

    public static UpdatePayoutDetailsRequest updateRequest() {
        return UpdatePayoutDetailsRequest.builder()
                .id(123L)
                .amount(new BigDecimal("300.00"))
                .requestedById("user-1")
                .paymentMode("bank")
                .callbackUrl("https://callback.example/payout")
                .paymentInstrument(PaymentInstrument.builder()
                        .accountNumber("1234567890")
                        .ifscCode("HDFC0001234")
                        .accountHolderName("Ada Lovelace")
                        .build())
                .build();
    }
}
