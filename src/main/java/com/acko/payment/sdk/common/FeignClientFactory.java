package com.acko.payment.sdk.common;

import com.acko.payment.sdk.config.TimeoutSettings;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.Feign;
import feign.Request;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.slf4j.Slf4jLogger;

import java.util.concurrent.TimeUnit;

/**
 * Builds package-private Feign clients with shared Jackson + auth interceptor.
 */
public final class FeignClientFactory {

    private final ObjectMapper objectMapper;
    private final PaymentFeignInterceptor interceptor;

    public FeignClientFactory(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper == null ? defaultObjectMapper() : objectMapper;
        this.interceptor = new PaymentFeignInterceptor();
    }

    public FeignClientFactory() {
        this(defaultObjectMapper());
    }

    public <T> T create(Class<T> type, String baseUrl, TimeoutSettings timeout) {
        Request.Options options = new Request.Options(
                timeout.getConnect().toMillis(), TimeUnit.MILLISECONDS,
                timeout.getRead().toMillis(), TimeUnit.MILLISECONDS,
                true);

        return Feign.builder()
                .options(options)
                .encoder(new JacksonEncoder(objectMapper))
                .decoder(new JacksonDecoder(objectMapper))
                .requestInterceptor(interceptor)
                .logger(new Slf4jLogger(type))
                .logLevel(feign.Logger.Level.BASIC)
                .target(type, trimTrailingSlash(baseUrl));
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public static ObjectMapper defaultObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    private static String trimTrailingSlash(String baseUrl) {
        if (baseUrl == null) {
            return null;
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}
