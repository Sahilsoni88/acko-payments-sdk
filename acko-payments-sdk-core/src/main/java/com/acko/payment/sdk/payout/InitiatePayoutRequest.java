package com.acko.payment.sdk.payout;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class InitiatePayoutRequest {

    @NotBlank
    private String okind;

    @NotNull
    private String oid;

    @NotNull
    private BigDecimal amount;

    private String creatorNotes;

    @NotBlank
    private String entityType;

    @NotNull
    private Long entityId;

    @NotNull
    @Valid
    private PaymentInstrument paymentInstrument;

    @NotBlank
    private String paymentMode;

    private String entitySubtype;
    private String callbackUrl;

    /** Idempotency key when supported by the platform. */
    private String uniqueId;

    @Builder.Default
    private String paymentType = "claim";

    private Long parentPaymentId;
    private Long paymentTaskId;

    private String requestedById;
    private String payoutRequestId;
    private String payoutLob;
}
