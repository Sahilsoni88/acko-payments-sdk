package com.acko.payment.sdk.util;

/**
 * Safe masking helpers for logs. Never log full account numbers or secrets.
 */
public final class MaskingUtils {

    private MaskingUtils() {
    }

    public static String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.isBlank()) {
            return "***";
        }
        String trimmed = accountNumber.trim();
        if (trimmed.length() <= 4) {
            return "****";
        }
        return "****" + trimmed.substring(trimmed.length() - 4);
    }

    public static String maskSecret(String value) {
        if (value == null || value.isBlank()) {
            return "***";
        }
        return "***";
    }

    public static String maskIfsc(String ifsc) {
        if (ifsc == null || ifsc.length() < 4) {
            return "****";
        }
        return ifsc.substring(0, 4) + "*******";
    }
}
