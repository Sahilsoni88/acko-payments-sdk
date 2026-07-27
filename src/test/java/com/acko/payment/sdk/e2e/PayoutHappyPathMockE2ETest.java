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
    void should_executeAllPayoutOperationsEndToEnd_andReuseCachedToken() {
        // given
        server.enqueue(ok("""
                {"access_token":"test-token","token_type":"Bearer","expires_in":3600}
                """));
        server.enqueue(ok("""
                {"payout_request_id":"payout-123"}
                """));
        server.enqueue(ok("""
                {"ifsc_code":"HDFC0001234","bank_name":"HDFC Bank","branch_name":"Indiranagar","is_valid":true}
                """));
        server.enqueue(ok("""
                {
                  "validation_status":"valid",
                  "message":"account valid",
                  "account_holder_name":"Ada Lovelace",
                  "validation_source":"nbin"
                }
                """));
        server.enqueue(ok("""
                {
                  "payout_request_id":"payout-123",
                  "status":"initiated",
                  "amount":250.50,
                  "request_id":"req-1",
                  "verification":{"payee_name":"Ada Lovelace","verified_name":"Ada Lovelace","result":"success"},
                  "validation":{"result":"success","message":"valid"}
                }
                """));
        server.enqueue(ok("""
                {"payout_request_id":"payout-123","status":"updated","amount":300.00,"message":"updated"}
                """));
        server.enqueue(ok("""
                {
                  "payout_request_id":"payout-123",
                  "status":"success",
                  "amount":300.00,
                  "gateway_transaction_id":"gw-1",
                  "verification_status":"success",
                  "verification_message":"settled"
                }
                """));

        PaymentClient client = client();

        // when
        GeneratePayoutRequestIdResponse id = client.payout().generatePayoutRequestId();
        VerifyIfscResponse ifsc = client.payout().verifyIfsc("HDFC0001234");
        ValidateAccountDetailsResponse account = client.payout().validateAccountDetails(validateAccountRequest());
        InitiatePayoutResponse initiated = client.payout().initiate(initiateRequest(id.getPayoutRequestId()));
        UpdatePayoutDetailsResponse updated = client.payout().updatePayoutDetails(updateRequest());
        VerifyPayoutResponse verified = client.payout().verify(id.getPayoutRequestId());

        // then
        assertThat(id.getPayoutRequestId()).isEqualTo("payout-123");
        assertThat(ifsc.getIsValid()).isTrue();
        assertThat(account.getValidationStatus()).isEqualTo("valid");
        assertThat(initiated.getStatus()).isEqualTo("initiated");
        assertThat(updated.getMessage()).isEqualTo("updated");
        assertThat(verified.getPaymentStatus()).isEqualTo(PaymentStatus.SUCCESS);

        List<RecordedRequest> requests = server.requests();
        assertThat(requests).hasSize(7);
        assertRequest(requests.get(0), "POST", "/oauth/token", null);
        assertThat(requests.get(0).body()).contains("grant_type=client_credentials");
        assertThat(requests.get(0).body()).contains("client_id=client-id");
        assertThat(requests.get(0).body()).contains("client_secret=client-secret");
        assertRequest(requests.get(1), "POST", "/api/generate_payout_request_id", AUTHORIZATION);
        assertRequest(requests.get(2), "GET", "/api/ifsc-verify?ifsc=HDFC0001234", AUTHORIZATION);
        assertRequest(requests.get(3), "POST", "/api/validate/account_details", AUTHORIZATION);
        assertRequest(requests.get(4), "POST", "/api/v2/initiate_payout", AUTHORIZATION);
        assertRequest(requests.get(5), "POST", "/api/v2/update_payout_details", AUTHORIZATION);
        assertRequest(requests.get(6), "GET", "/api/payout-123/verify", AUTHORIZATION);
        assertThat(requests.get(4).body()).contains("\"payment_type\":\"claim\"");
        assertThat(requests.get(4).body()).contains("\"payment_instrument\"");
        assertThat(requests.get(4).body()).contains("\"unique_id\":\"idem-1\"");
    }
}
