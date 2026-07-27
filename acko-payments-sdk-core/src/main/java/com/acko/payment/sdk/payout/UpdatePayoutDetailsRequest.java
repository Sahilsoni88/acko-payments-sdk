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
public class UpdatePayoutDetailsRequest {

    @NotNull
    private Long id;

    @NotNull
    private BigDecimal amount;

    @NotNull
    @Valid
    private PaymentInstrument paymentInstrument;

    @NotBlank
    private String requestedById;

    @NotBlank
    private String paymentMode;

    @NotBlank
    private String callbackUrl;
}
