package com.acko.payment.sdk.e2e;

import com.acko.payment.sdk.api.PaymentClient;
import com.acko.payment.sdk.e2e.support.MockE2ETestSupport;
import com.acko.payment.sdk.e2e.support.RecordedRequest;
import com.acko.payment.sdk.model.PaymentStatus;
import com.acko.payment.sdk.payout.GeneratePayoutRequestIdResponse;
import com.acko.payment.sdk.payout.InitiatePayoutResponse;
import com.acko.payment.sdk.payout.UpdatePayoutDetailsResponse;
import com.acko.payment.sdk.payout.ValidateAccountDetailsResponse;
import com.acko.payment.sdk.payout.VerifyIfscResponse;
import com.acko.payment.sdk.payout.VerifyPayoutResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.acko.payment.sdk.e2e.support.HttpRequestAssertions.assertRequest;
import static com.acko.payment.sdk.e2e.support.MockResponses.ok;
import static com.acko.payment.sdk.e2e.support.PayoutFixtures.initiateRequest;
import static com.acko.payment.sdk.e2e.support.PayoutFixtures.updateRequest;
import static com.acko.payment.sdk.e2e.support.PayoutFixtures.validateAccountRequest;
import static org.assertj.core.api.Assertions.assertThat;

class PayoutHappyPathMockE2ETest extends MockE2ETestSupport {

    @Test
    void should_executeAllPayoutOperationsEndToEnd_withPayoutCookie() {
        // given
        server.enqueue(ok("""
                {"payout_request_id":"payout-123"}
                """));
        server.enqueue(ok("""
                {
                  "success": true,
                  "data": {
                    "ifsc": "HDFC0001234",
                    "name": "HDFC Bank",
                    "address": "Indiranagar",
                    "city": "Bengaluru",
                    "state": "Karnataka"
                  }
                }
                """));
        server.enqueue(ok("""
                {
                  "match_ratio": 100.0,
                  "verified_account_holder_name":"Ada Lovelace"
                }
                """));
        server.enqueue(ok("""
                {
                  "success": true,
                  "result": {
                    "id": "payment-ekey-1",
                    "message": null
                  }
                }
                """));
        server.enqueue(ok("""
                {
                  "success": true,
                  "result": {
                    "id": "payment-ekey-v1",
                    "message": null
                  }
                }
                """));
        server.enqueue(ok("""
                {
                  "success": true,
                  "result": {
                    "id": 123,
                    "oid": 42,
                    "okind": "jarvis",
                    "amount": "300.00",
                    "status": "created",
                    "createdOn": "2026-07-27T01:00:00Z",
                    "updatedOn": "2026-07-27T01:05:00Z",
                    "paymentInstrument": "{\\"account_number\\":\\"1234567890\\"}",
                    "paymentMode": "bank",
                    "payoutRequestId": "payout-123",
                    "paymentType": "claim"
                  }
                }
                """));
        server.enqueue(ok("""
                {
                  "oid": 42,
                  "okind": "jarvis",
                  "payout_request_id":"payout-123",
                  "status":"completed",
                  "amount":300.00,
                  "created_on": "2026-07-27T01:00:00Z",
                  "updated_on": "2026-07-27T01:05:00Z",
                  "payment_instrument": {"account_number":"1234567890"},
                  "payment_mode": "bank",
                  "utr":"utr-1",
                  "retry_flag":"N"
                }
                """));

        PaymentClient client = client();

        // when
        GeneratePayoutRequestIdResponse id = client.payout().generatePayoutRequestId();
        VerifyIfscResponse ifsc = client.payout().verifyIfsc("HDFC0001234");
        ValidateAccountDetailsResponse account = client.payout().validateAccountDetails(validateAccountRequest());
        InitiatePayoutResponse initiated = client.payout().initiate(initiateRequest(id.getPayoutRequestId()));
        InitiatePayoutResponse initiatedV1 = client.payout().initiateV1(initiateRequest(id.getPayoutRequestId()));
        UpdatePayoutDetailsResponse updated = client.payout().updatePayoutDetails(updateRequest());
        VerifyPayoutResponse verified = client.payout().verify(id.getPayoutRequestId());

        // then
        assertThat(id.getPayoutRequestId()).isEqualTo("payout-123");
        assertThat(ifsc.getIsValid()).isTrue();
        assertThat(ifsc.getIfscCode()).isEqualTo("HDFC0001234");
        assertThat(ifsc.getBankName()).isEqualTo("HDFC Bank");
        assertThat(account.getMatchRatio()).isEqualTo(100.0);
        assertThat(account.getVerifiedAccountHolderName()).isEqualTo("Ada Lovelace");
        assertThat(initiated.getId()).isEqualTo("payment-ekey-1");
        assertThat(initiatedV1.getId()).isEqualTo("payment-ekey-v1");
        assertThat(updated.getStatus()).isEqualTo("created");
        assertThat(updated.getPayoutRequestId()).isEqualTo("payout-123");
        assertThat(verified.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);

        List<RecordedRequest> requests = server.requests();
        assertThat(requests).hasSize(7);
        assertRequest(requests.get(0), "POST", "/api/generate_payout_request_id", PAYOUT_COOKIE);
        assertRequest(requests.get(1), "GET", "/api/ifsc-verify?ifsc=HDFC0001234", PAYOUT_COOKIE);
        assertRequest(requests.get(2), "POST", "/api/validate/account_details", PAYOUT_COOKIE);
        assertRequest(requests.get(3), "POST", "/api/v2/initiate_payout", PAYOUT_COOKIE);
        assertRequest(requests.get(4), "POST", "/api/initiate_payout/", PAYOUT_COOKIE);
        assertRequest(requests.get(5), "POST", "/api/v2/update_payout_details", PAYOUT_COOKIE);
        assertRequest(requests.get(6), "GET", "/api/payout-123/verify", PAYOUT_COOKIE);
        assertThat(requests.get(3).body()).contains("\"payment_type\":\"claim\"");
        assertThat(requests.get(3).body()).contains("\"payment_instrument\"");
        assertThat(requests.get(3).body()).contains("\"unique_id\":\"idem-1\"");
        assertThat(requests.get(3).body()).contains("\"entity_id\":101");
        assertThat(requests.get(3).body()).contains("\"payment_mode\":\"bank\"");
        assertThat(requests.get(3).body()).contains("\"ifsc\":\"HDFC0001234\"");
        assertThat(requests.get(3).body()).doesNotContain("ifsc_code");
        assertThat(requests.get(4).body()).contains("\"payment_mode\":\"bank\"");
    }
}
