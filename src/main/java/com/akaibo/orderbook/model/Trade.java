package com.akaibo.orderbook.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

public record Trade(String id, BigDecimal price, BigDecimal quantity, String currencyPair, Instant tradedAt,
                    String takerSide, BigDecimal quoteVolume) {
    public Trade(String id, BigDecimal price, BigDecimal quantity, String currencyPair, Instant tradedAt, String takerSide,
                 BigDecimal quoteVolume) {
        this.id = id;
        this.price = price.setScale(4, RoundingMode.HALF_UP);
        this.quantity = quantity.setScale(8, RoundingMode.HALF_UP);
        this.currencyPair = currencyPair;
        this.tradedAt = tradedAt;
        this.takerSide = takerSide;
        this.quoteVolume = quoteVolume.setScale(12, RoundingMode.HALF_UP);
    }
}
