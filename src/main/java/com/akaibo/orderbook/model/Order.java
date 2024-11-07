package com.akaibo.orderbook.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

public class Order {
    private final String id;
    private final BigDecimal price;
    private BigDecimal quantity;
    private final String pair;
    private final OrderSide side;
    private final Instant timestamp; // Timestamp of order creation

    public Order(String id, BigDecimal price, BigDecimal quantity, String pair, OrderSide side, Instant timestamp) {
        this.id = id;
        this.price = price.setScale(4, RoundingMode.HALF_UP);
        this.quantity = quantity.setScale(8, RoundingMode.HALF_UP);
        this.pair = pair;
        this.side = side;
        this.timestamp = timestamp;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getPair() {
        return pair;
    }

    public OrderSide getSide() {
        return side;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public enum OrderSide {
        BUY, SELL
    }

}
