package com.acko.payment.sdk.common;

/**
 * Thread-local bearer token for Feign request interception.
 */
public final class AccessTokenHolder {

    private static final ThreadLocal<String> TOKEN = new ThreadLocal<>();

    private AccessTokenHolder() {
    }

    public static void set(String authorizationHeader) {
        TOKEN.set(authorizationHeader);
    }

    public static String get() {
        return TOKEN.get();
    }

    public static void clear() {
        TOKEN.remove();
    }
}
