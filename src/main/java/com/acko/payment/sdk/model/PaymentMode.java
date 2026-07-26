package com.acko.payment.sdk.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Supported payout payment modes.
 */
public enum PaymentMode {
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
