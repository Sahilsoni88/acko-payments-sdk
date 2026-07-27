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
                .okind("jarvis")
                .oid("42")
                .paymentType("claim")
                .amount(new BigDecimal("250.50"))
                .requestedById("user-1")
                .entityType("customer")
                .entityId(101L)
                .callbackUrl("https://cb")
                .paymentMode("bank")
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
        assertThat(json).contains("\"okind\":\"jarvis\"");
        assertThat(json).contains("\"payment_type\":\"claim\"");
        assertThat(json).contains("\"requested_by_id\":\"user-1\"");
        assertThat(json).contains("\"payment_instrument\"");
        assertThat(json).contains("\"account_number\":\"1234567890\"");
        assertThat(json).contains("\"ifsc\":\"HDFC0001234\"");
        assertThat(json).contains("\"account_holder\":\"Ada Lovelace\"");
        assertThat(json).contains("\"payout_request_id\":\"po-1\"");
        assertThat(json).contains("\"unique_id\":\"idem-1\"");
        assertThat(json).doesNotContain("ifsc_code");
        assertThat(json).doesNotContain("accountNumber");
    }

    @Test
    void should_deserializeVerifyResponse_fromSnakeCase() throws Exception {
        // given
        String json = """
                {
                  "oid": 42,
                  "okind": "jarvis",
                  "payout_request_id": "po-99",
                  "status": "completed",
                  "amount": 100.00,
                  "created_on": "2026-07-27T01:00:00Z",
                  "updated_on": "2026-07-27T01:05:00Z",
                  "payment_instrument": {"account_number": "1234567890"},
                  "payment_mode": "bank",
                  "failure_reason": "none",
                  "utr": "utr-1",
                  "retry_flag": "N"
                }
                """;

        // when
        VerifyPayoutResponse response = objectMapper.readValue(json, VerifyPayoutResponse.class);

        // then
        assertThat(response.getPayoutRequestId()).isEqualTo("po-99");
        assertThat(response.getStatus()).isEqualTo("completed");
        assertThat(response.getGatewayTransactionId()).isEqualTo("utr-1");
        assertThat(response.getCreatedAt()).isEqualTo("2026-07-27T01:00:00Z");
        assertThat(response.getPaymentStatus().name()).isEqualTo("COMPLETED");
    }

    @Test
    void should_deserializeInitiateResponse_fromActualPlatformWrapper() throws Exception {
        // given
        String json = """
                {
                  "success": true,
                  "result": {
                    "id": "payment-ekey-1",
                    "message": null
                  }
                }
                """;

        // when
        InitiatePayoutResponse response = objectMapper.readValue(json, InitiatePayoutResponse.class);

        // then
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getId()).isEqualTo("payment-ekey-1");
        assertThat(response.getResult().getId()).isEqualTo("payment-ekey-1");
    }

    @Test
    void should_deserializeUpdateResponse_fromActualPlatformWrapper() throws Exception {
        // given
        String json = """
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
                    "paymentType": "claim",
                    "retryPaymentFlag": true
                  }
                }
                """;

        // when
        UpdatePayoutDetailsResponse response = objectMapper.readValue(json, UpdatePayoutDetailsResponse.class);

        // then
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getId()).isEqualTo(123L);
        assertThat(response.getAmount()).isEqualByComparingTo("300.00");
        assertThat(response.getPayoutRequestId()).isEqualTo("payout-123");
        assertThat(response.getPaymentMode()).isEqualTo("bank");
        assertThat(response.getRetryPaymentFlag()).isTrue();
    }

    @Test
    void should_deserializeVerifyIfscResponse_fromActualPlatformShape() throws Exception {
        // given
        String json = """
                {
                  "success": true,
                  "data": {
                    "ifsc": "SBIN0017118",
                    "name": "State Bank of India",
                    "address": "VILLAGE AND POST KHARGONE,TEHSIL BARELI,DISTT.RAISEN.MADHYA PRADESH 464671",
                    "city": "BARELI",
                    "state": "MADHYA PRADESH"
                  }
                }
                """;

        // when
        VerifyIfscResponse response = objectMapper.readValue(json, VerifyIfscResponse.class);

        // then
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getIsValid()).isTrue();
        assertThat(response.getIfscCode()).isEqualTo("SBIN0017118");
        assertThat(response.getBankName()).isEqualTo("State Bank of India");
        assertThat(response.getData().getCity()).isEqualTo("BARELI");
        assertThat(response.getData().getState()).isEqualTo("MADHYA PRADESH");
    }

    @Test
    void should_roundTripValidateAccountDetailsRequest() throws Exception {
        // given
        ValidateAccountDetailsRequest request = ValidateAccountDetailsRequest.builder()
                .accountNumber("999988887777")
                .ifscCode("SBIN0000001")
                .accountHolderName("Test")
                .accountType("bank")
                .build();

        // when
        String json = objectMapper.writeValueAsString(request);
        ValidateAccountDetailsRequest roundTrip =
                objectMapper.readValue(json, ValidateAccountDetailsRequest.class);

        // then
        assertThat(roundTrip.getIfsc()).isEqualTo("SBIN0000001");
        assertThat(roundTrip.getIfscCode()).isEqualTo("SBIN0000001");
        assertThat(json).contains("account_holder_name");
        assertThat(json).contains("\"ifsc\":\"SBIN0000001\"");
        assertThat(json).contains("\"account_type\":\"bank\"");
        assertThat(json).doesNotContain("ifsc_code");
    }
}
