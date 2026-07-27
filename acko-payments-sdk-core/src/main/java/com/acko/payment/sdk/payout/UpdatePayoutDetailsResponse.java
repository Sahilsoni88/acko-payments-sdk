package com.acko.payment.sdk.payout;

import com.fasterxml.jackson.annotation.JsonAlias;
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
public class UpdatePayoutDetailsResponse {

    private Boolean success;
    private Result result;
    private Long id;
    private Long oid;
    private String okind;
    private String payoutRequestId;
    private String status;
    private BigDecimal amount;
    private String message;
    private String createdOn;
    private String updatedOn;
    private String requestedById;
    private String creatorNotes;
    private String utr;
    private Long entityId;
    private String entityType;
    private String entitySubtype;
    private String paymentInstrument;
    private String paymentMode;
    private String transactingBank;
    private String requestNo;
    private String callbackUrl;
    private String uniqueResponseNo;
    private String checksum;
    private String uniqueId;
    private String parentUniqueId;
    private String paymentType;
    private Long parentPaymentId;
    private String failureReason;
    private String source;
    private String webhookSignature;
    private Boolean retryPaymentFlag;
    private String webhook;

    @JsonProperty("result")
    public void setResult(Result result) {
        this.result = result;
        if (result == null) {
            return;
        }
        this.id = result.getId();
        this.oid = result.getOid();
        this.okind = result.getOkind();
        this.payoutRequestId = result.getPayoutRequestId();
        this.status = result.getStatus();
        this.amount = result.getAmount();
        this.createdOn = result.getCreatedOn();
        this.updatedOn = result.getUpdatedOn();
        this.requestedById = result.getRequestedById();
        this.creatorNotes = result.getCreatorNotes();
        this.utr = result.getUtr();
        this.entityId = result.getEntityId();
        this.entityType = result.getEntityType();
        this.entitySubtype = result.getEntitySubtype();
        this.paymentInstrument = result.getPaymentInstrument();
        this.paymentMode = result.getPaymentMode();
        this.transactingBank = result.getTransactingBank();
        this.requestNo = result.getRequestNo();
        this.callbackUrl = result.getCallbackUrl();
        this.uniqueResponseNo = result.getUniqueResponseNo();
        this.checksum = result.getChecksum();
        this.uniqueId = result.getUniqueId();
        this.parentUniqueId = result.getParentUniqueId();
        this.paymentType = result.getPaymentType();
        this.parentPaymentId = result.getParentPaymentId();
        this.failureReason = result.getFailureReason();
        this.source = result.getSource();
        this.webhookSignature = result.getWebhookSignature();
        this.retryPaymentFlag = result.getRetryPaymentFlag();
        this.webhook = result.getWebhook();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        private Long id;
        private Long oid;
        private String okind;
        private BigDecimal amount;
        private String status;
        @JsonAlias("createdOn")
        private String createdOn;
        @JsonAlias("updatedOn")
        private String updatedOn;
        @JsonAlias("requestedById")
        private String requestedById;
        @JsonAlias("creatorNotes")
        private String creatorNotes;
        private String utr;
        @JsonAlias("entityId")
        private Long entityId;
        @JsonAlias("entityType")
        private String entityType;
        @JsonAlias("entitySubtype")
        private String entitySubtype;
        @JsonAlias("paymentInstrument")
        private String paymentInstrument;
        @JsonAlias("paymentMode")
        private String paymentMode;
        @JsonAlias("transactingBank")
        private String transactingBank;
        @JsonAlias("requestNo")
        private String requestNo;
        @JsonAlias("callbackUrl")
        private String callbackUrl;
        @JsonAlias("uniqueResponseNo")
        private String uniqueResponseNo;
        private String checksum;
        @JsonAlias("uniqueId")
        private String uniqueId;
        @JsonAlias("payoutRequestId")
        private String payoutRequestId;
        @JsonAlias("parentUniqueId")
        private String parentUniqueId;
        @JsonAlias("paymentType")
        private String paymentType;
        @JsonAlias("parentPaymentId")
        private Long parentPaymentId;
        @JsonAlias("failureReason")
        private String failureReason;
        private String source;
        @JsonAlias("webhookSignature")
        private String webhookSignature;
        @JsonAlias("retryPaymentFlag")
        private Boolean retryPaymentFlag;
        private String webhook;
    }
}
