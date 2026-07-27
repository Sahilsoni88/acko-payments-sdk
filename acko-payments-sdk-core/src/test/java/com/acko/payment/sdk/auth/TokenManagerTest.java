package com.acko.payment.sdk.auth;

import com.acko.payment.sdk.config.TokenCacheSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenManagerTest {

    private static final String CACHE_KEY = "acko-payment-sdk:oauth-token";

    @Mock
    private AuthService authService;

    @Mock
    private TokenStore tokenStore;

    private TokenManager tokenManager;

    @BeforeEach
    void setUp() {
        tokenManager = new TokenManager(
                authService,
                tokenStore,
                new TokenCacheSettings(Duration.ofSeconds(30), CACHE_KEY));
    }

    @Test
    void should_returnCachedToken_whenTokenValid() {
        // given
        OAuthToken cached = new OAuthToken("token-1", "Bearer", Instant.now().plusSeconds(3600));
        when(tokenStore.get(CACHE_KEY)).thenReturn(Optional.of(cached));

        // when
        OAuthToken result = tokenManager.getValidToken();

        // then
        assertThat(result).isSameAs(cached);
        verify(tokenStore).get(CACHE_KEY);
        verify(authService, never()).fetchToken();
        verifyNoMoreInteractions(authService, tokenStore);
    }

    @Test
    void should_refreshToken_whenCacheMiss() {
        // given
        OAuthToken fresh = new OAuthToken("token-2", "Bearer", Instant.now().plusSeconds(3600));
        when(tokenStore.get(CACHE_KEY)).thenReturn(Optional.empty());
        when(authService.fetchToken()).thenReturn(fresh);

        // when
        OAuthToken result = tokenManager.getValidToken();

        // then
        assertThat(result).isSameAs(fresh);
        verify(tokenStore, times(2)).get(CACHE_KEY);
        verify(authService).fetchToken();
        verify(tokenStore).store(eq(CACHE_KEY), eq(fresh));
        verifyNoMoreInteractions(authService, tokenStore);
    }

    @Test
    void should_refreshToken_whenNearExpiry() {
        // given
        OAuthToken expired = new OAuthToken("old", "Bearer", Instant.now().plusSeconds(10));
        OAuthToken fresh = new OAuthToken("new", "Bearer", Instant.now().plusSeconds(3600));
        when(tokenStore.get(CACHE_KEY)).thenReturn(Optional.of(expired));
        when(authService.fetchToken()).thenReturn(fresh);

        // when
        OAuthToken result = tokenManager.getValidToken();

        // then
        assertThat(result.getAccessToken()).isEqualTo("new");
        verify(authService).fetchToken();
        verify(tokenStore).store(CACHE_KEY, fresh);
    }

    @Test
    void should_invalidateCache_whenInvalidateCalled() {
        // given
        // when
        tokenManager.invalidate();

        // then
        verify(tokenStore).invalidate(CACHE_KEY);
        verifyNoMoreInteractions(tokenStore);
        verify(authService, never()).fetchToken();
    }
}
