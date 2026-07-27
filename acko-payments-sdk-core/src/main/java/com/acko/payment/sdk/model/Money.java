package com.acko.payment.sdk.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

/**
 * Monetary amount with currency. Defaults to INR.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public final class Money {

    private final BigDecimal amount;
    private final String currency;

    @JsonCreator
    public Money(
            @JsonProperty("amount") BigDecimal amount,
            @JsonProperty("currency") String currency) {
        this.amount = Objects.requireNonNull(amount, "amount");
        this.currency = currency == null || currency.isBlank() ? "INR" : currency;
    }

    public static Money ofInr(BigDecimal amount) {
        return new Money(amount, "INR");
    }

    public static Money ofInr(double amount) {
        return ofInr(BigDecimal.valueOf(amount));
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public Currency toCurrency() {
        return Currency.getInstance(currency);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Money money)) {
            return false;
        }
        return amount.compareTo(money.amount) == 0 && currency.equals(money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.stripTrailingZeros(), currency);
    }

    @Override
    public String toString() {
        return amount + " " + currency;
    }
}
