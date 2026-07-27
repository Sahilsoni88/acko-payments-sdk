package com.acko.payment.sdk.payout;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ValidateAccountDetailsResponse {

    private Double matchRatio;
    private String verifiedAccountHolderName;

    private String validationStatus;
    private String message;
    private String accountHolderName;
    private String validationSource;
}
