package com.acko.payment.sdk.spring;

import com.acko.payment.sdk.api.PaymentClient;
import com.acko.payment.sdk.auth.InMemoryTokenStore;
import com.acko.payment.sdk.auth.TokenStore;
import com.acko.payment.sdk.common.MetricsHook;
import com.acko.payment.sdk.config.SdkConfig;
import com.acko.payment.sdk.factory.PaymentClientFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot auto-configuration for the Payment SDK.
 * Enabled when {@code payment.auth.token-url} and {@code payment.payout.base-url} are set.
 */
@AutoConfiguration
@ConditionalOnClass(PaymentClient.class)
@EnableConfigurationProperties(PaymentProperties.class)
@ConditionalOnProperty(prefix = "payment", name = {"auth.token-url", "payout.base-url"})
public class PaymentAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TokenStore paymentTokenStore() {
        return new InMemoryTokenStore();
    }

    @Bean
    @ConditionalOnMissingBean
    public MetricsHook paymentMetricsHook() {
        return MetricsHook.NOOP;
    }

    @Bean
    @ConditionalOnMissingBean
    public SdkConfig paymentSdkConfig(PaymentProperties properties) {
        return PaymentPropertiesMapper.toSdkConfig(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public PaymentClient paymentClient(
            SdkConfig sdkConfig,
            TokenStore paymentTokenStore,
            MetricsHook paymentMetricsHook) {
        return PaymentClientFactory.create(sdkConfig, paymentTokenStore, paymentMetricsHook, null);
    }
}
