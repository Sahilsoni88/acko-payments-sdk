package com.acko.payment.sdk.e2e;

import com.acko.payment.sdk.api.PaymentClient;
import com.acko.payment.sdk.e2e.support.MockE2ETestSupport;
import com.acko.payment.sdk.e2e.support.RecordedRequest;
import com.acko.payment.sdk.exception.AuthenticationException;
import com.acko.payment.sdk.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.acko.payment.sdk.e2e.support.HttpRequestAssertions.assertRequest;
import static com.acko.payment.sdk.e2e.support.MockResponses.json;
import static com.acko.payment.sdk.e2e.support.PayoutFixtures.validateAccountRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PayoutErrorMappingMockE2ETest extends MockE2ETestSupport {

    @Test
    void should_mapClientErrorToValidationException_withoutRetry() {
        // given
        server.enqueue(json(400, "{\"error\":\"bad account\"}"));

        PaymentClient client = client();

        // when / then
        assertThatThrownBy(() -> client.payout().validateAccountDetails(validateAccountRequest()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("bad account");

        List<RecordedRequest> requests = server.requests();
        assertThat(requests).hasSize(1);
        assertRequest(requests.get(0), "POST", "/api/validate/account_details", PAYOUT_COOKIE);
    }

    @Test
    void should_failBeforePlatformCall_whenPayoutCookieMissing() {
        // given
        PaymentClient client = clientWithoutPayoutCookie();

        // when / then
        assertThatThrownBy(() -> client.payout().verify("payout-123"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("payment.payout.cookie-header");

        List<RecordedRequest> requests = server.requests();
        assertThat(requests).isEmpty();
    }
}
