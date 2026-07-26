package com.acko.payment.sdk.payout;

import com.acko.payment.sdk.common.FeignClientFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PayoutDtoSerializationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = FeignClientFactory.defaultObjectMapper();
    }

    @Test
    void should_serializeInitiateRequest_withSnakeCase() throws Exception {
        // given
        InitiatePayoutRequest request = InitiatePayoutRequest.builder()
                .okind("claims")
                .oid(42L)
                .paymentType("claim")
                .amount(new BigDecimal("250.50"))
                .requestedById("user-1")
                .entityType("customer")
                .entityId("e-1")
                .callbackUrl("https://cb")
                .paymentMode("neft")
                .payoutRequestId("po-1")
                .uniqueId("idem-1")
                .paymentInstrument(PaymentInstrument.builder()
                        .accountNumber("1234567890")
                        .ifscCode("HDFC0001234")
                        .accountHolderName("Ada Lovelace")
                        .accountType("savings")
                        .build())
                .build();

        // when
        String json = objectMapper.writeValueAsString(request);

        // then
        assertThat(json).contains("\"okind\":\"claims\"");
        assertThat(json).contains("\"payment_type\":\"claim\"");
        assertThat(json).contains("\"requested_by_id\":\"user-1\"");
        assertThat(json).contains("\"payment_instrument\"");
        assertThat(json).contains("\"account_number\":\"1234567890\"");
        assertThat(json).contains("\"ifsc_code\":\"HDFC0001234\"");
        assertThat(json).contains("\"payout_request_id\":\"po-1\"");
        assertThat(json).contains("\"unique_id\":\"idem-1\"");
        assertThat(json).doesNotContain("accountNumber");
    }

    @Test
    void should_deserializeVerifyResponse_fromSnakeCase() throws Exception {
        // given
        String json = """
                {
                  "payout_request_id": "po-99",
                  "status": "success",
                  "amount": 100.00,
                  "gateway_transaction_id": "txn-1",
                  "verification_status": "ok"
                }
                """;

        // when
        VerifyPayoutResponse response = objectMapper.readValue(json, VerifyPayoutResponse.class);

        // then
        assertThat(response.getPayoutRequestId()).isEqualTo("po-99");
        assertThat(response.getStatus()).isEqualTo("success");
        assertThat(response.getGatewayTransactionId()).isEqualTo("txn-1");
        assertThat(response.getPaymentStatus().name()).isEqualTo("SUCCESS");
    }

    @Test
    void should_roundTripValidateAccountDetailsRequest() throws Exception {
        // given
        ValidateAccountDetailsRequest request = ValidateAccountDetailsRequest.builder()
                .accountNumber("999988887777")
                .ifscCode("SBIN0000001")
                .accountHolderName("Test")
                .build();

        // when
        String json = objectMapper.writeValueAsString(request);
        ValidateAccountDetailsRequest roundTrip =
                objectMapper.readValue(json, ValidateAccountDetailsRequest.class);

        // then
        assertThat(roundTrip).isEqualTo(request);
        assertThat(json).contains("account_holder_name");
    }
}
