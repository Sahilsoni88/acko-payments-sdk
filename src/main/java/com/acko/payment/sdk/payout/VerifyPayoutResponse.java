package com.acko.payment.sdk.payout;

import com.acko.payment.sdk.model.PaymentStatus;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class VerifyPayoutResponse {

    private String payoutRequestId;
    private String status;
    private BigDecimal amount;
    private String gatewayTransactionId;
    private String gatewayResponse;
    private String errorMessage;
    private String createdAt;
    private String updatedAt;
    private String verificationStatus;
    private String verificationMessage;

    public PaymentStatus getPaymentStatus() {
        return PaymentStatus.fromValue(status);
    }
}
