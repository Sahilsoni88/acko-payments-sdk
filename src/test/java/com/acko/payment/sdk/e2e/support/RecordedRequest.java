package com.acko.payment.sdk.e2e.support;

import com.sun.net.httpserver.Headers;

public record RecordedRequest(String method, String path, Headers headers, String body) {
}
