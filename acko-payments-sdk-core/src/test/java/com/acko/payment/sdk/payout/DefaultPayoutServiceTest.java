package com.acko.payment.sdk.payout;

import com.acko.payment.sdk.common.RequestContext;
import com.acko.payment.sdk.common.RequestExecutor;
import com.acko.payment.sdk.config.RetrySettings;
import com.acko.payment.sdk.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultPayoutServiceTest {

    @Mock
    private PayoutFeignClient feignClient;

    @Mock
    private RequestExecutor requestExecutor;

    private DefaultPayoutService service;

    @BeforeEach
    void setUp() {
        service = new DefaultPayoutService(feignClient, requestExecutor, RetrySettings.defaults());
    }

    @Test
    void should_generatePayoutRequestId_whenCalled() {
        // given
        GeneratePayoutRequestIdResponse expected = GeneratePayoutRequestIdResponse.builder()
                .payoutRequestId("po-1")
                .build();
        when(requestExecutor.execute(any(RequestContext.class), any()))
                .thenAnswer(invocation -> {
                    Supplier<?> supplier = invocation.getArgument(1);
                    return supplier.get();
                });
        when(feignClient.generatePayoutRequestId()).thenReturn(expected);

        // when
        GeneratePayoutRequestIdResponse result = service.generatePayoutRequestId();

        // then
        assertThat(result.getPayoutRequestId()).isEqualTo("po-1");
        ArgumentCaptor<RequestContext> contextCaptor = ArgumentCaptor.forClass(RequestContext.class);
        verify(requestExecutor).execute(contextCaptor.capture(), any());
        assertThat(contextCaptor.getValue().getOperation()).isEqualTo("generatePayoutRequestId");
        assertThat(contextCaptor.getValue().isRetrySafe()).isTrue();
        verify(feignClient).generatePayoutRequestId();
        verifyNoMoreInteractions(requestExecutor, feignClient);
    }

    @Test
    void should_throwValidationException_whenIfscBlank() {
        // given
        // when / then
        assertThatThrownBy(() -> service.verifyIfsc("  "))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("ifsc");

        verifyNoInteractions(requestExecutor, feignClient);
    }

    @Test
    void should_initiateWithoutRetrySafe_whenMutating() {
        // given
        InitiatePayoutRequest request = InitiatePayoutRequest.builder()
                .okind("claims")
                .oid("123")
                .paymentType("claim")
                .amount(new BigDecimal("100.00"))
                .requestedById("user-1")
                .entityType("customer")
                .entityId(101L)
                .callbackUrl("https://example.com/cb")
                .paymentMode("bank")
                .paymentInstrument(PaymentInstrument.builder()
                        .accountNumber("1234567890")
                        .ifscCode("HDFC0001234")
                        .accountHolderName("Test User")
                        .build())
                .build();
        InitiatePayoutResponse expected = InitiatePayoutResponse.builder()
                .payoutRequestId("po-9")
                .status("initiated")
                .build();
        when(requestExecutor.execute(any(RequestContext.class), any()))
                .thenAnswer(invocation -> {
                    Supplier<?> supplier = invocation.getArgument(1);
                    return supplier.get();
                });
        when(feignClient.initiate(request)).thenReturn(expected);

        // when
        InitiatePayoutResponse result = service.initiate(request);

        // then
        assertThat(result.getPayoutRequestId()).isEqualTo("po-9");
        ArgumentCaptor<RequestContext> contextCaptor = ArgumentCaptor.forClass(RequestContext.class);
        verify(requestExecutor).execute(contextCaptor.capture(), any());
        assertThat(contextCaptor.getValue().isRetrySafe()).isFalse();
        verify(feignClient).initiate(request);
        verifyNoMoreInteractions(requestExecutor, feignClient);
    }

    @Test
    void should_initiateV1WithoutRetrySafe_whenMutating() {
        // given
        InitiatePayoutRequest request = InitiatePayoutRequest.builder()
                .okind("jarvis")
                .oid("123")
                .paymentType("claim")
                .amount(new BigDecimal("100.00"))
                .requestedById("user-1")
                .entityType("customer")
                .entityId(101L)
                .callbackUrl("https://example.com/cb")
                .paymentMode("bank")
                .paymentInstrument(PaymentInstrument.builder()
                        .accountNumber("1234567890")
                        .ifscCode("HDFC0001234")
                        .accountHolderName("Test User")
                        .build())
                .build();
        InitiatePayoutResponse expected = InitiatePayoutResponse.builder()
                .id("payment-ekey-1")
                .build();
        when(requestExecutor.execute(any(RequestContext.class), any()))
                .thenAnswer(invocation -> {
                    Supplier<?> supplier = invocation.getArgument(1);
                    return supplier.get();
                });
        when(feignClient.initiateV1(request)).thenReturn(expected);

        // when
        InitiatePayoutResponse result = service.initiateV1(request);

        // then
        assertThat(result.getId()).isEqualTo("payment-ekey-1");
        ArgumentCaptor<RequestContext> contextCaptor = ArgumentCaptor.forClass(RequestContext.class);
        verify(requestExecutor).execute(contextCaptor.capture(), any());
        assertThat(contextCaptor.getValue().getOperation()).isEqualTo("initiateV1");
        assertThat(contextCaptor.getValue().isRetrySafe()).isFalse();
        verify(feignClient).initiateV1(request);
        verifyNoMoreInteractions(requestExecutor, feignClient);
    }

    @Test
    void should_throwValidationException_whenVerifyIdNull() {
        // given
        // when / then
        assertThatThrownBy(() -> service.verify(null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("payoutRequestId");

        verifyNoInteractions(requestExecutor, feignClient);
    }

    @Test
    void should_verifyPayout_whenValidId() {
        // given
        VerifyPayoutResponse expected = VerifyPayoutResponse.builder()
                .payoutRequestId("po-1")
                .status("success")
                .build();
        when(requestExecutor.execute(any(RequestContext.class), any()))
                .thenAnswer(invocation -> {
                    Supplier<?> supplier = invocation.getArgument(1);
                    return supplier.get();
                });
        when(feignClient.verify("po-1")).thenReturn(expected);

        // when
        VerifyPayoutResponse result = service.verify("po-1");

        // then
        assertThat(result.getPaymentStatus().getValue()).isEqualTo("success");
        verify(feignClient).verify("po-1");
        verify(requestExecutor).execute(any(RequestContext.class), any());
        verifyNoMoreInteractions(requestExecutor, feignClient);
    }
}
