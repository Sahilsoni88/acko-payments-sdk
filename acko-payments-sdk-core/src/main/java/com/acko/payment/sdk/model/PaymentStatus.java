package com.acko.payment.sdk.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Normalized payment / payout status values used by the SDK.
 */
public enum PaymentStatus {
    APPROVED("approved"),
    CREATED("created"),
    INITIATED("initiated"),
    INITIATION_FAILED("initiation_failed"),
    REVERSED("reversed"),
    COMPLETED("completed"),
    PAYOUT_INITIATED("payout_initiated"),
    IN_PROCESS("in-process"),
    PENDING("pending"),
    SUCCESS("success"),
    FAILED("failed"),
    REJECTED("rejected"),
    DUPLICATE("duplicate"),
    PENDING_FROM_CUSTOMER("pending-from-customer"),
    RETRY_BY_PAYOUT_SERVICE("retry_by_payout_service"),
    RETRY_IN_PROCESS("retry_in_process"),
    MANUALLY_PROCESSED("manually-processed"),
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
