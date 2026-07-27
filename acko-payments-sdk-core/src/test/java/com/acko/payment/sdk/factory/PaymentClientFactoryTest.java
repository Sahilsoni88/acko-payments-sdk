package com.acko.payment.sdk.factory;

import com.acko.payment.sdk.api.PaymentClient;
import com.acko.payment.sdk.config.SdkConfig;
import com.acko.payment.sdk.config.ServiceSettings;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentClientFactoryTest {

    @Test
    void should_createPaymentClient_whenValidConfig() {
        // given
        SdkConfig config = SdkConfig.builder()
                .payout(new ServiceSettings(
                        "https://payout.example",
                        null,
                        null,
                        "internalPayoutCookie=test-cookie"))
                .build();

        // when
        PaymentClient client = PaymentClientFactory.create(config);

        // then
        assertThat(client).isNotNull();
        assertThat(client.payout()).isNotNull();
    }
}
