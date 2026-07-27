package com.acko.payment.sdk.common;

final class CookieHolder {

    private static final ThreadLocal<String> COOKIE = new ThreadLocal<>();

    private CookieHolder() {
    }

    static void set(String cookieHeader) {
        COOKIE.set(cookieHeader);
    }

    static String get() {
        return COOKIE.get();
    }

    static void clear() {
        COOKIE.remove();
    }
}
