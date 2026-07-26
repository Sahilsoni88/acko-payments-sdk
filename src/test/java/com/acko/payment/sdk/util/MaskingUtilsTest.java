package com.acko.payment.sdk.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MaskingUtilsTest {

    @Test
    void should_maskAccountNumber_keepingLastFour() {
        // given
        // when
        String masked = MaskingUtils.maskAccountNumber("1234567890");

        // then
        assertThat(masked).isEqualTo("****7890");
    }

    @Test
    void should_maskSecret_always() {
        // given
        // when / then
        assertThat(MaskingUtils.maskSecret("super-secret")).isEqualTo("***");
    }
}
