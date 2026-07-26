package com.acko.payment.sdk.common;

/**
 * Optional metrics callback. Default is a no-op.
 */
public interface MetricsHook {

    MetricsHook NOOP = new MetricsHook() {
    };

    default void onSuccess(String operation, long latencyMs) {
    }

    default void onFailure(String operation, long latencyMs, String errorCode) {
    }
}
