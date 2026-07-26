package com.acko.payment.sdk.common;

import com.acko.payment.sdk.config.RetrySettings;
import com.acko.payment.sdk.exception.DownstreamException;
import com.acko.payment.sdk.exception.RetryableException;
import com.acko.payment.sdk.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RetryExecutorTest {

    private RetryExecutor retryExecutor;

    @BeforeEach
    void setUp() {
        retryExecutor = new RetryExecutor(new ExceptionMapper());
    }

    @Test
    void should_returnResult_whenCallSucceeds() {
        // given
        RequestContext context = new RequestContext(
                "verify", "corr-1", new RetrySettings(true, 3, Duration.ofMillis(1)), true);

        // when
        String result = retryExecutor.execute(context, () -> "ok");

        // then
        assertThat(result).isEqualTo("ok");
    }

    @Test
    void should_retryAndSucceed_whenTransientThenOk() {
        // given
        AtomicInteger attempts = new AtomicInteger();
        RequestContext context = new RequestContext(
                "verifyIfsc", "corr-1", new RetrySettings(true, 3, Duration.ofMillis(1)), true);

        // when
        String result = retryExecutor.execute(context, () -> {
            if (attempts.incrementAndGet() < 2) {
                throw new RetryableException("transient");
            }
            return "ok";
        });

        // then
        assertThat(result).isEqualTo("ok");
        assertThat(attempts.get()).isEqualTo(2);
    }

    @Test
    void should_failWithoutRetry_whenNotRetrySafe() {
        // given
        AtomicInteger attempts = new AtomicInteger();
        RequestContext context = new RequestContext(
                "initiate", "corr-1", new RetrySettings(true, 3, Duration.ofMillis(1)), false);

        // when / then
        assertThatThrownBy(() -> retryExecutor.execute(context, () -> {
            attempts.incrementAndGet();
            throw new RetryableException("transient");
        })).isInstanceOf(DownstreamException.class);

        assertThat(attempts.get()).isEqualTo(1);
    }

    @Test
    void should_notRetry_whenValidationException() {
        // given
        AtomicInteger attempts = new AtomicInteger();
        RequestContext context = new RequestContext(
                "verify", "corr-1", new RetrySettings(true, 3, Duration.ofMillis(1)), true);

        // when / then
        assertThatThrownBy(() -> retryExecutor.execute(context, () -> {
            attempts.incrementAndGet();
            throw new ValidationException("bad");
        })).isInstanceOf(ValidationException.class);

        assertThat(attempts.get()).isEqualTo(1);
    }
}
