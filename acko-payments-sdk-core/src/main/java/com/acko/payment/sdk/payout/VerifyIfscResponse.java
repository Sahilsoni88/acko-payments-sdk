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
public class VerifyIfscResponse {

    /**
     * Actual Central Payment Platform IFSC response shape.
     */
    private Boolean success;
    private IfscData data;

    /**
     * Legacy / flattened shape kept for backward-compatible callers and tests.
     */
    private String ifscCode;
    private String bankName;
    private String branchName;
    private Boolean isValid;
    private String errorMessage;

    public String getIfscCode() {
        return ifscCode != null ? ifscCode : data == null ? null : data.getIfsc();
    }

    public String getBankName() {
        return bankName != null ? bankName : data == null ? null : data.getName();
    }

    public Boolean getIsValid() {
        return isValid != null ? isValid : success;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class IfscData {

        private String ifsc;
        private String name;
        private String address;
        private String city;
        private String state;
    }
}
