package com.acko.payment.sdk.payout;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Beneficiary bank account details for payout.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PaymentInstrument {

    @NotBlank
    private String accountNumber;

    @NotBlank
    private String ifscCode;

    @NotBlank
    private String accountHolderName;

    private String accountType;

    private String beneficiaryId;
}
