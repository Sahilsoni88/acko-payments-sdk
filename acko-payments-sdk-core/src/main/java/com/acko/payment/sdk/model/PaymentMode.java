package com.acko.payment.sdk.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Supported payout payment modes.
 */
public enum PaymentMode {
    BANK_ACCOUNT("bank"),
    BENEFICIARY_ID("beneficiary_id"),
    PAYTM("paytm"),
    UPI("upi"),
    AMAZON_PAY("amazon_pay"),

    /**
     * Legacy values retained for source compatibility. Payout-service accepts
     * {@code bank}, {@code beneficiary_id}, {@code paytm}, {@code upi}, and
     * {@code amazon_pay}.
     */
    NEFT("neft"),
    RTGS("rtgs"),
    IMPS("imps"),
    BANK_TRANSFER("bank_transfer");

    private final String value;

    PaymentMode(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static PaymentMode fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (PaymentMode mode : values()) {
            if (mode.value.equalsIgnoreCase(value) || mode.name().equalsIgnoreCase(value)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Unknown payment mode: " + value);
    }
}
