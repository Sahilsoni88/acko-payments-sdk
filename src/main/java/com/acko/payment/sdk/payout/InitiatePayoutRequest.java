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
    private Object oid;

    @NotBlank
    private String paymentType;

    @NotNull
    private BigDecimal amount;

    @NotBlank
    private String requestedById;

    @NotBlank
    private String entityType;

    @NotBlank
    private String entityId;

    private String entitySubtype;

    @NotBlank
    private String callbackUrl;

    @NotBlank
    private String paymentMode;

    @NotNull
    @Valid
    private PaymentInstrument paymentInstrument;

    private Long parentPaymentId;

    private String payoutRequestId;

    /** Idempotency key when supported by the platform. */
    private String uniqueId;
}
