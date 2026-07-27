package com.acko.payment.sdk.common;

/**
 * Thread-local bearer token for Feign request interception.
 */
final class AccessTokenHolder {

    private static final ThreadLocal<String> TOKEN = new ThreadLocal<>();

    private AccessTokenHolder() {
    }

    static void set(String authorizationHeader) {
        TOKEN.set(authorizationHeader);
    }

    static String get() {
        return TOKEN.get();
    }

    static void clear() {
        TOKEN.remove();
    }
}
