package com.acko.payment.sdk.spring;

import com.acko.payment.sdk.api.PaymentClient;
import com.acko.payment.sdk.auth.TokenStore;
import com.acko.payment.sdk.config.SdkConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(PaymentAutoConfiguration.class))
            .withPropertyValues(
                    "payment.payout.base-url=https://payout.example",
                    "payment.payout.cookie-header=internalPayoutCookie=test-cookie");

    @Test
    void should_loadPaymentClientBean_whenRequiredPropertiesPresent() {
        // given / when / then
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(PaymentClient.class);
            assertThat(context).hasSingleBean(SdkConfig.class);
            assertThat(context).hasSingleBean(TokenStore.class);
            assertThat(context.getBean(SdkConfig.class).getPayout().getBaseUrl())
                    .isEqualTo("https://payout.example");
            assertThat(context.getBean(SdkConfig.class).getPayout().getCookieHeader())
                    .isEqualTo("internalPayoutCookie=test-cookie");
        });
    }

    @Test
    void should_notLoadAutoConfig_whenPayoutCookieHeaderDisabled() {
        // given
        ApplicationContextRunner runner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(PaymentAutoConfiguration.class))
                .withPropertyValues(
                        "payment.payout.base-url=https://payout.example",
                        "payment.payout.cookie-header=false");

        // when / then
        runner.run(context -> assertThat(context).doesNotHaveBean(PaymentClient.class));
    }
}
