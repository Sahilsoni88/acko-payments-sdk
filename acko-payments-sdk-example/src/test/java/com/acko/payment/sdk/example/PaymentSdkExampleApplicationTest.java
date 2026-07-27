package com.acko.payment.sdk.example;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "payment.payout.base-url=http://localhost:9999",
        "payment.payout.cookie-header=internalPayoutCookie=test"
})
class PaymentSdkExampleApplicationTest {

    @Test
    void contextLoads() {
    }
}
