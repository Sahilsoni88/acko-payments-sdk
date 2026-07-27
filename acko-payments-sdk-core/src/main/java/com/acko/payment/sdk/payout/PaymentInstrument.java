package com.acko.payment.sdk.payout;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

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

    @JsonAlias("ifsc_code")
    private String ifsc;

    @JsonAlias("account_holder_name")
    private String accountHolder;

    @JsonProperty("lob_reference_no")
    @JsonAlias("claim_number")
    private String lobReferenceNo;

    private String userPhone;
    private String userEmail;
    private String inputEmail;
    private String vpa;

    @JsonAlias("transferMode")
    private String transferMode;

    /**
     * Backward-compatible builder/read field. Serialized as {@code ifsc}.
     */
    @JsonIgnore
    private String ifscCode;

    /**
     * Backward-compatible builder/read field. Serialized as {@code account_holder}.
     */
    @JsonIgnore
    private String accountHolderName;

    @JsonIgnore
    private String accountType;

    @JsonIgnore
    private String beneficiaryId;

    @JsonIgnore
    @Builder.Default
    private Map<String, Object> additionalProperties = new HashMap<>();

    public String getIfsc() {
        return ifsc != null ? ifsc : ifscCode;
    }

    public String getIfscCode() {
        return ifscCode != null ? ifscCode : ifsc;
    }

    public String getAccountHolder() {
        return accountHolder != null ? accountHolder : accountHolderName;
    }

    public String getAccountHolderName() {
        return accountHolderName != null ? accountHolderName : accountHolder;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        additionalProperties.put(name, value);
    }
}
