package com.acko.payment.sdk.common;

import com.acko.payment.sdk.exception.AuthenticationException;
import com.acko.payment.sdk.exception.DownstreamException;
import com.acko.payment.sdk.exception.PaymentTimeoutException;
import com.acko.payment.sdk.exception.RetryableException;
import com.acko.payment.sdk.exception.ValidationException;
import feign.Request;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionMapperTest {

    private ExceptionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ExceptionMapper();
    }

    @Test
    void should_mapToValidationException_whenHttp400() {
        // given
        FeignException exception = feignStatus(400, "bad request");

        // when
        RuntimeException mapped = mapper.map(exception);

        // then
        assertThat(mapped).isInstanceOf(ValidationException.class);
        assertThat(((ValidationException) mapped).getHttpStatus()).isEqualTo(400);
    }

    @Test
    void should_mapToAuthenticationException_whenHttp401() {
        // given
        FeignException exception = feignStatus(401, "unauthorized");

        // when
        RuntimeException mapped = mapper.map(exception);

        // then
        assertThat(mapped).isInstanceOf(AuthenticationException.class);
    }

    @Test
    void should_mapToRetryableException_whenHttp500() {
        // given
        FeignException exception = feignStatus(500, "boom");

        // when
        RuntimeException mapped = mapper.map(exception);

        // then
        assertThat(mapped).isInstanceOf(RetryableException.class);
    }

    @Test
    void should_mapToTimeoutException_whenSocketTimeout() {
        // given
        RuntimeException cause = new RuntimeException(new SocketTimeoutException("read timed out"));

        // when
        RuntimeException mapped = mapper.map(cause);

        // then
        assertThat(mapped).isInstanceOf(PaymentTimeoutException.class);
    }

    @Test
    void should_mapToDownstream_whenUnknownCheckedWrapped() {
        // given
        Exception cause = new Exception("weird");

        // when
        RuntimeException mapped = mapper.map(cause);

        // then
        assertThat(mapped).isInstanceOf(DownstreamException.class);
    }

    private static FeignException feignStatus(int status, String body) {
        Request request = Request.create(
                Request.HttpMethod.GET,
                "http://localhost/test",
                Collections.emptyMap(),
                null,
                StandardCharsets.UTF_8,
                null);
        return FeignException.errorStatus(
                "test",
                feign.Response.builder()
                        .status(status)
                        .reason("error")
                        .request(request)
                        .headers(Collections.emptyMap())
                        .body(body, StandardCharsets.UTF_8)
                        .build());
    }
}
