package com.acko.payment.sdk.common;

import com.acko.payment.sdk.auth.OAuthToken;
import com.acko.payment.sdk.auth.TokenManager;
import com.acko.payment.sdk.config.RetrySettings;
import com.acko.payment.sdk.exception.AuthenticationException;
import com.acko.payment.sdk.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestExecutorTest {

    @Mock
    private TokenManager tokenManager;

    @Mock
    private MetricsHook metricsHook;

    private RequestExecutor requestExecutor;

    @BeforeEach
    void setUp() {
        ExceptionMapper exceptionMapper = new ExceptionMapper();
        RetryExecutor retryExecutor = new RetryExecutor(exceptionMapper);
        requestExecutor = new RequestExecutor(tokenManager, retryExecutor, exceptionMapper, metricsHook);
    }

    @Test
    void should_executeAndClearToken_whenSuccess() {
        // given
        when(tokenManager.getValidToken())
                .thenReturn(new OAuthToken("abc", "Bearer", Instant.now().plusSeconds(60)));
        RequestContext context = new RequestContext(
                "verify", "corr-1", RetrySettings.defaults(), true);

        // when
        String result = requestExecutor.execute(context, () -> {
            assertThat(AccessTokenHolder.get()).isEqualTo("Bearer abc");
            return "done";
        });

        // then
        assertThat(result).isEqualTo("done");
        assertThat(AccessTokenHolder.get()).isNull();
        verify(tokenManager).getValidToken();
        verify(metricsHook).onSuccess(eq("verify"), anyLong());
        verifyNoMoreInteractions(tokenManager, metricsHook);
    }

    @Test
    void should_clearTokenAndRecordFailure_whenValidationFails() {
        // given
        when(tokenManager.getValidToken())
                .thenReturn(new OAuthToken("abc", "Bearer", Instant.now().plusSeconds(60)));
        RequestContext context = new RequestContext(
                "initiate", "corr-1", new RetrySettings(false, 1, Duration.ofMillis(1)), false);

        // when / then
        assertThatThrownBy(() -> requestExecutor.execute(context, () -> {
            throw new ValidationException("bad payload");
        })).isInstanceOf(ValidationException.class);

        assertThat(AccessTokenHolder.get()).isNull();
        verify(tokenManager).getValidToken();
        verify(metricsHook).onFailure(eq("initiate"), anyLong(), eq("VALIDATION_ERROR"));
        verifyNoMoreInteractions(tokenManager, metricsHook);
    }

    @Test
    void should_executeWithCookieAndNoBearerToken_whenCookieAuthenticated() {
        // given
        ExceptionMapper exceptionMapper = new ExceptionMapper();
        RequestExecutor cookieExecutor = RequestExecutor.cookieAuthenticated(
                "internalPayoutCookie=test-cookie",
                new RetryExecutor(exceptionMapper),
                exceptionMapper,
                metricsHook);
        RequestContext context = new RequestContext(
                "verifyIfsc", "corr-1", RetrySettings.defaults(), true);

        // when
        String result = cookieExecutor.execute(context, () -> {
            assertThat(CookieHolder.get()).isEqualTo("internalPayoutCookie=test-cookie");
            assertThat(AccessTokenHolder.get()).isNull();
            return "done";
        });

        // then
        assertThat(result).isEqualTo("done");
        assertThat(CookieHolder.get()).isNull();
        verify(metricsHook).onSuccess(eq("verifyIfsc"), anyLong());
        verifyNoMoreInteractions(metricsHook);
    }

    @Test
    void should_failBeforeCall_whenCookieMissing() {
        // given
        ExceptionMapper exceptionMapper = new ExceptionMapper();
        RequestExecutor cookieExecutor = RequestExecutor.cookieAuthenticated(
                null,
                new RetryExecutor(exceptionMapper),
                exceptionMapper,
                metricsHook);
        RequestContext context = new RequestContext(
                "verifyIfsc", "corr-1", RetrySettings.defaults(), true);

        // when / then
        assertThatThrownBy(() -> cookieExecutor.execute(context, () -> "done"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("payment.payout.cookie-header");

        assertThat(CookieHolder.get()).isNull();
        verify(metricsHook).onFailure(eq("verifyIfsc"), anyLong(), eq("AUTHENTICATION_ERROR"));
        verifyNoMoreInteractions(metricsHook);
    }
}
