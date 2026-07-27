package com.acko.payment.sdk.e2e.support;

import static org.assertj.core.api.Assertions.assertThat;

public final class HttpRequestAssertions {

    private HttpRequestAssertions() {
    }

    public static void assertRequest(
            RecordedRequest request,
            String method,
            String path,
            String authorization) {
        assertThat(request.method()).isEqualTo(method);
        assertThat(request.path()).isEqualTo(path);
        if (authorization == null) {
            assertThat(request.headers().getFirst("Authorization")).isNull();
        } else {
            assertThat(request.headers().getFirst("Authorization")).isEqualTo(authorization);
        }
    }
}
