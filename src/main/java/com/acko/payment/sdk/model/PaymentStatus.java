package com.acko.payment.sdk.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Normalized payment / payout status values used by the SDK.
 */
public enum PaymentStatus {
    CREATED("created"),
    INITIATED("initiated"),
    PAYOUT_INITIATED("payout_initiated"),
    PENDING("pending"),
    SUCCESS("success"),
    FAILED("failed"),
    UNKNOWN("unknown");

    private final String value;

    PaymentStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static PaymentStatus fromValue(String value) {
        if (value == null || value.isBlank()) {
            return UNKNOWN;
        }
        for (PaymentStatus status : values()) {
            if (status.value.equalsIgnoreCase(value) || status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        return UNKNOWN;
    }
}
