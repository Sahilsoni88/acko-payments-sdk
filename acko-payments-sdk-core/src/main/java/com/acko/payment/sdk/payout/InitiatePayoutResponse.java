package com.acko.payment.sdk.payout;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class InitiatePayoutResponse {

    private Boolean success;
    private Result result;
    private String id;
    private String message;
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

    @JsonProperty("result")
    public void setResult(Result result) {
        this.result = result;
        if (result == null) {
            return;
        }
        this.id = result.getId();
        this.message = result.getMessage();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        private String id;
        private String message;
    }
}
