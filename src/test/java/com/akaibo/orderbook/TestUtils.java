package com.akaibo.orderbook;

import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(VertxExtension.class)
public class TestUtils {

    // Utility method to enforce financial precision
    private BigDecimal roundPrice(BigDecimal price) {
        return price.setScale(4, RoundingMode.HALF_UP); // Rounds price to 4 decimal places, rounding half up
    }

    private BigDecimal roundQuantity(BigDecimal quantity) {
        return quantity.setScale(8, RoundingMode.HALF_UP); // Rounds quantity to 8 decimal places, rounding half up
    }

    @Test
    public void testFinancialPrecision() {
        BigDecimal price = new BigDecimal("125.5678656");
        BigDecimal quantity = new BigDecimal("1.2345678948");

        BigDecimal roundedPrice = roundPrice(price);
        BigDecimal roundedQuantity = roundQuantity(quantity);

        assertEquals(new BigDecimal("125.5679"), roundedPrice); // Expected rounded price
        assertEquals(new BigDecimal("1.23456789"), roundedQuantity); // Expected rounded quantity
    }
}
