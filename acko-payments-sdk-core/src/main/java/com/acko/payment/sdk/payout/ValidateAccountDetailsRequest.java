package com.acko.payment.sdk.payout;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ValidateAccountDetailsRequest {

    @NotBlank
    private String accountNumber;

    @JsonAlias("ifsc_code")
    private String ifsc;

    /**
     * Backward-compatible builder/read field. Serialized as {@code ifsc}.
     */
    @JsonIgnore
    private String ifscCode;

    @NotBlank
    private String accountHolderName;

    private String vpa;

    @NotBlank
    private String accountType;

    private String payoutRequestType;

    @JsonIgnore
    private String entityId;

    public String getIfsc() {
        return ifsc != null ? ifsc : ifscCode;
    }

    public String getIfscCode() {
        return ifscCode != null ? ifscCode : ifsc;
    }
}
