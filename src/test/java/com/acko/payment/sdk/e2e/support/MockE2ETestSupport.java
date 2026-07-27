package com.acko.payment.sdk.e2e.support;

import com.acko.payment.sdk.api.PaymentClient;
import com.acko.payment.sdk.config.AuthSettings;
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

    protected static final String AUTHORIZATION = "Bearer test-token";

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
        SdkConfig config = SdkConfig.builder()
                .auth(new AuthSettings(server.url("/oauth/token"), "client-id", "client-secret", "payment.write"))
                .payout(new ServiceSettings(
                        server.url(""),
                        timeoutSettings,
                        new RetrySettings(true, 3, Duration.ofMillis(1))))
                .build();
        return PaymentClientFactory.create(config);
    }
}
