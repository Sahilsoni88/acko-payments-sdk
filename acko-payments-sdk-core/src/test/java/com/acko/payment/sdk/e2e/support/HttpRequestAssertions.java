package com.acko.payment.sdk.e2e.support;

import static org.assertj.core.api.Assertions.assertThat;

public final class HttpRequestAssertions {

    private HttpRequestAssertions() {
    }

    public static void assertRequest(
            RecordedRequest request,
            String method,
            String path,
            String cookie) {
        assertThat(request.method()).isEqualTo(method);
        assertThat(request.path()).isEqualTo(path);
        assertThat(request.headers().getFirst("Authorization")).isNull();
        assertThat(request.headers().getFirst("Cookie")).isEqualTo(cookie);
    }
}
