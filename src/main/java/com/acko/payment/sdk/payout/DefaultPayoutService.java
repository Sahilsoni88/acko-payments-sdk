package com.acko.payment.sdk.payout;

import com.acko.payment.sdk.api.PayoutOperations;
import com.acko.payment.sdk.common.RequestContext;
import com.acko.payment.sdk.common.RequestExecutor;
import com.acko.payment.sdk.config.RetrySettings;
import com.acko.payment.sdk.exception.ValidationException;
import com.acko.payment.sdk.util.CorrelationIdHolder;

import java.util.Objects;

/**
 * Package-private payout operations implementation.
 */
final class DefaultPayoutService implements PayoutOperations {

    private final PayoutFeignClient feignClient;
    private final RequestExecutor requestExecutor;
    private final RetrySettings retrySettings;

    DefaultPayoutService(
            PayoutFeignClient feignClient,
            RequestExecutor requestExecutor,
            RetrySettings retrySettings) {
        this.feignClient = Objects.requireNonNull(feignClient, "feignClient");
        this.requestExecutor = Objects.requireNonNull(requestExecutor, "requestExecutor");
        this.retrySettings = Objects.requireNonNull(retrySettings, "retrySettings");
    }

    @Override
    public GeneratePayoutRequestIdResponse generatePayoutRequestId() {
        return requestExecutor.execute(
                context("generatePayoutRequestId", true),
                feignClient::generatePayoutRequestId);
    }

    @Override
    public VerifyIfscResponse verifyIfsc(String ifsc) {
        if (ifsc == null || ifsc.isBlank()) {
            throw new ValidationException("ifsc must not be blank");
        }
        return requestExecutor.execute(
                context("verifyIfsc", true),
                () -> feignClient.verifyIfsc(ifsc.trim()));
    }

    @Override
    public ValidateAccountDetailsResponse validateAccountDetails(ValidateAccountDetailsRequest request) {
        requireNonNull(request, "request");
        return requestExecutor.execute(
                context("validateAccountDetails", true),
                () -> feignClient.validateAccountDetails(request));
    }

    @Override
    public InitiatePayoutResponse initiate(InitiatePayoutRequest request) {
        requireNonNull(request, "request");
        // Money-moving: no blind retry — consumers must verify on timeout.
        return requestExecutor.execute(
                context("initiate", false),
                () -> feignClient.initiate(request));
    }

    @Override
    public UpdatePayoutDetailsResponse updatePayoutDetails(UpdatePayoutDetailsRequest request) {
        requireNonNull(request, "request");
        return requestExecutor.execute(
                context("updatePayoutDetails", false),
                () -> feignClient.updatePayoutDetails(request));
    }

    @Override
    public VerifyPayoutResponse verify(String payoutRequestId) {
        if (payoutRequestId == null || payoutRequestId.isBlank()) {
            throw new ValidationException("payoutRequestId must not be blank");
        }
        return requestExecutor.execute(
                context("verify", true),
                () -> feignClient.verify(payoutRequestId.trim()));
    }

    private RequestContext context(String operation, boolean retrySafe) {
        return new RequestContext(
                operation,
                CorrelationIdHolder.get().orElse(null),
                retrySettings,
                retrySafe);
    }

    private static void requireNonNull(Object value, String name) {
        if (value == null) {
            throw new ValidationException(name + " must not be null");
        }
    }
}
