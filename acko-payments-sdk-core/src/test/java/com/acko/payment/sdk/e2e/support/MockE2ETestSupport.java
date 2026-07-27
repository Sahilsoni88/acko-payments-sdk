package com.acko.payment.sdk.e2e.support;

import com.acko.payment.sdk.api.PaymentClient;
import com.acko.payment.sdk.config.RetrySettings;
import com.acko.payment.sdk.config.SdkConfig;
import com.acko.payment.sdk.config.ServiceSettings;
import com.acko.payment.sdk.config.TimeoutSettings;
import com.acko.payment.sdk.factory.PaymentClientFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.time.Duration;

public abstract class MockE2ETestSupport {

    protected static final String PAYOUT_COOKIE = "internalPayoutCookie=test-cookie";

    protected FakePaymentServer server;

    @BeforeEach
    void setUpFakeServer() throws IOException {
        server = new FakePaymentServer();
        server.start();
    }

    @AfterEach
    void tearDownFakeServer() {
        server.stop();
    }

    protected PaymentClient client() {
        return client(TimeoutSettings.defaults());
    }

    protected PaymentClient client(TimeoutSettings timeoutSettings) {
        return client(timeoutSettings, PAYOUT_COOKIE);
    }

    protected PaymentClient clientWithoutPayoutCookie() {
        return client(TimeoutSettings.defaults(), null);
    }

    private PaymentClient client(TimeoutSettings timeoutSettings, String cookieHeader) {
        SdkConfig config = SdkConfig.builder()
                .payout(new ServiceSettings(
                        server.url(""),
                        timeoutSettings,
                        new RetrySettings(true, 3, Duration.ofMillis(1)),
                        cookieHeader))
                .build();
        return PaymentClientFactory.create(config);
    }
}
