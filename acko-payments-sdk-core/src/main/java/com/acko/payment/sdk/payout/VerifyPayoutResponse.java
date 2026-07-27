package com.acko.payment.sdk.payout;

import com.acko.payment.sdk.model.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class VerifyPayoutResponse {

    private Long oid;
    private String okind;
    private BigDecimal amount;
    private String status;
    private String createdOn;
    private String updatedOn;
    private String requestedById;
    private String creatorNotes;
    private Map<String, Object> paymentInstrument;
    private String paymentMode;
    private String payoutRequestId;
    private String paymentType;
    private String failureReason;
    private String source;
    private String utr;
    private String retryFlag;

    @JsonAlias("utr")
    private String gatewayTransactionId;
    private String gatewayResponse;
    @JsonAlias("failure_reason")
    private String errorMessage;
    @JsonAlias("created_on")
    private String createdAt;
    @JsonAlias("updated_on")
    private String updatedAt;
    private String verificationStatus;
    private String verificationMessage;

    public String getGatewayTransactionId() {
        return gatewayTransactionId != null ? gatewayTransactionId : utr;
    }

    public String getErrorMessage() {
        return errorMessage != null ? errorMessage : failureReason;
    }

    public String getCreatedAt() {
        return createdAt != null ? createdAt : createdOn;
    }

    public String getUpdatedAt() {
        return updatedAt != null ? updatedAt : updatedOn;
    }

    public PaymentStatus getPaymentStatus() {
        return PaymentStatus.fromValue(status);
    }
}
