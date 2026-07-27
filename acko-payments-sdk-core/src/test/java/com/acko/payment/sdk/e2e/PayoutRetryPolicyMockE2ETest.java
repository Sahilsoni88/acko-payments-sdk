package com.acko.payment.sdk.e2e;

import com.acko.payment.sdk.api.PaymentClient;
import com.acko.payment.sdk.config.TimeoutSettings;
import com.acko.payment.sdk.e2e.support.MockE2ETestSupport;
import com.acko.payment.sdk.e2e.support.RecordedRequest;
import com.acko.payment.sdk.exception.DownstreamException;
import com.acko.payment.sdk.exception.PaymentTimeoutException;
import com.acko.payment.sdk.model.PaymentStatus;
import com.acko.payment.sdk.payout.VerifyPayoutResponse;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static com.acko.payment.sdk.e2e.support.HttpRequestAssertions.assertRequest;
import static com.acko.payment.sdk.e2e.support.MockResponses.delayedJson;
import static com.acko.payment.sdk.e2e.support.MockResponses.json;
import static com.acko.payment.sdk.e2e.support.MockResponses.ok;
import static com.acko.payment.sdk.e2e.support.PayoutFixtures.initiateRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PayoutRetryPolicyMockE2ETest extends MockE2ETestSupport {

    @Test
    void should_retrySafeOperation_whenServerErrorThenSuccess() {
        // given
        server.enqueue(json(500, "{\"error\":\"temporary\"}"));
        server.enqueue(ok("""
                {"payout_request_id":"payout-123","status":"success"}
                """));

        PaymentClient client = client();

        // when
        VerifyPayoutResponse response = client.payout().verify("payout-123");

        // then
        assertThat(response.getPaymentStatus()).isEqualTo(PaymentStatus.SUCCESS);
        List<RecordedRequest> requests = server.requests();
        assertThat(requests).hasSize(2);
        assertRequest(requests.get(0), "GET", "/api/payout-123/verify", PAYOUT_COOKIE);
        assertRequest(requests.get(1), "GET", "/api/payout-123/verify", PAYOUT_COOKIE);
    }

    @Test
    void should_retrySafeOperation_whenTimeoutThenSuccess() {
        // given
        server.enqueue(delayedJson(200, 300, """
                {"payout_request_id":"payout-123","status":"pending"}
                """));
        server.enqueue(ok("""
                {"payout_request_id":"payout-123","status":"success"}
                """));

        PaymentClient client = client(new TimeoutSettings(Duration.ofMillis(100), Duration.ofMillis(100)));

        // when
        VerifyPayoutResponse response = client.payout().verify("payout-123");

        // then
        assertThat(response.getPaymentStatus()).isEqualTo(PaymentStatus.SUCCESS);
        List<RecordedRequest> requests = server.requests();
        assertThat(requests.stream().filter(request -> request.path().equals("/api/payout-123/verify")))
                .hasSize(2);
    }

    @Test
    void should_notRetryInitiate_whenServerError() {
        // given
        server.enqueue(json(500, "{\"error\":\"temporary\"}"));

        PaymentClient client = client();

        // when / then
        assertThatThrownBy(() -> client.payout().initiate(initiateRequest("payout-123")))
                .isInstanceOf(DownstreamException.class)
                .hasMessageContaining("Retries exhausted");

        List<RecordedRequest> requests = server.requests();
        assertThat(requests).hasSize(1);
        assertRequest(requests.get(0), "POST", "/api/v2/initiate_payout", PAYOUT_COOKIE);
    }

    @Test
    void should_notRetryInitiateV1_whenServerError() {
        // given
        server.enqueue(json(500, "{\"error\":\"temporary\"}"));

        PaymentClient client = client();

        // when / then
        assertThatThrownBy(() -> client.payout().initiateV1(initiateRequest("payout-123")))
                .isInstanceOf(DownstreamException.class)
                .hasMessageContaining("Retries exhausted");

        List<RecordedRequest> requests = server.requests();
        assertThat(requests).hasSize(1);
        assertRequest(requests.get(0), "POST", "/api/initiate_payout/", PAYOUT_COOKIE);
    }

    @Test
    void should_notRetryInitiate_whenTimeout() {
        // given
        server.enqueue(delayedJson(200, 300, """
                {"payout_request_id":"payout-123","status":"initiated"}
                """));

        PaymentClient client = client(new TimeoutSettings(Duration.ofMillis(100), Duration.ofMillis(100)));

        // when / then
        assertThatThrownBy(() -> client.payout().initiate(initiateRequest("payout-123")))
                .isInstanceOf(PaymentTimeoutException.class);

        List<RecordedRequest> requests = server.requests();
        assertThat(requests.stream().filter(request -> request.path().equals("/api/v2/initiate_payout")))
                .hasSize(1);
    }
}
