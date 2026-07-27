package com.acko.payment.sdk.example;

import com.acko.payment.sdk.api.PaymentClient;
import com.acko.payment.sdk.payout.GeneratePayoutRequestIdResponse;
import com.acko.payment.sdk.payout.InitiatePayoutRequest;
import com.acko.payment.sdk.payout.InitiatePayoutResponse;
import com.acko.payment.sdk.payout.UpdatePayoutDetailsRequest;
import com.acko.payment.sdk.payout.UpdatePayoutDetailsResponse;
import com.acko.payment.sdk.payout.ValidateAccountDetailsRequest;
import com.acko.payment.sdk.payout.ValidateAccountDetailsResponse;
import com.acko.payment.sdk.payout.VerifyIfscResponse;
import com.acko.payment.sdk.payout.VerifyPayoutResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test/paymentsdk/payout")
public class PayoutTestController {

    private final PaymentClient paymentClient;

    public PayoutTestController(PaymentClient paymentClient) {
        this.paymentClient = paymentClient;
    }

    @GetMapping("/ifsc")
    public ResponseEntity<VerifyIfscResponse> verifyIfsc(
            @RequestParam(name = "ifsc", defaultValue = "SBIN0017118") String ifsc) {
        return ResponseEntity.ok(paymentClient.payout().verifyIfsc(ifsc));
    }

    @PostMapping("/request-id")
    public ResponseEntity<GeneratePayoutRequestIdResponse> generatePayoutRequestId() {
        return ResponseEntity.ok(paymentClient.payout().generatePayoutRequestId());
    }

    @PostMapping("/validate-account")
    public ResponseEntity<ValidateAccountDetailsResponse> validateAccountDetails(
            @RequestBody ValidateAccountDetailsRequest request) {
        return ResponseEntity.ok(paymentClient.payout().validateAccountDetails(request));
    }

    @PostMapping("/initiate-v2")
    public ResponseEntity<InitiatePayoutResponse> initiateV2(
            @RequestBody InitiatePayoutRequest request) {
        return ResponseEntity.ok(paymentClient.payout().initiate(request));
    }

    @PostMapping("/initiate-v1")
    public ResponseEntity<InitiatePayoutResponse> initiateV1(
            @RequestBody InitiatePayoutRequest request) {
        return ResponseEntity.ok(paymentClient.payout().initiateV1(request));
    }

    @PostMapping("/update")
    public ResponseEntity<UpdatePayoutDetailsResponse> updatePayoutDetails(
            @RequestBody UpdatePayoutDetailsRequest request) {
        return ResponseEntity.ok(paymentClient.payout().updatePayoutDetails(request));
    }

    @GetMapping("/{payoutRequestId}/verify")
    public ResponseEntity<VerifyPayoutResponse> verifyPayout(
            @PathVariable(name = "payoutRequestId") String payoutRequestId) {
        return ResponseEntity.ok(paymentClient.payout().verify(payoutRequestId));
    }
}
