package com.acko.payment.sdk.payout;

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
public class InitiatePayoutResponse {

    private String payoutRequestId;
    private String status;
    private BigDecimal amount;
    private String requestId;
    private VerificationDetails verification;
    private ValidationDetails validation;
    private String lob;
    private String journey;
    private String referenceId;
    private String redirectionUrl;
}
