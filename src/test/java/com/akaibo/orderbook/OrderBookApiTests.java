package com.akaibo.orderbook;

import com.akaibo.orderbook.api.OrderApiVerticle;
import com.akaibo.orderbook.model.Order;
import com.akaibo.orderbook.model.Trade;
import com.akaibo.orderbook.service.OrderBookService;
import com.akaibo.orderbook.service.TradeHistoryService;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(VertxExtension.class)
public class OrderBookApiTests {
    private Vertx vertx;
    private OrderBookService orderBookService;
    private TradeHistoryService tradeHistoryService;

    @BeforeEach
    public void setUp(VertxTestContext testContext) {
        vertx = Vertx.vertx();
        tradeHistoryService = new TradeHistoryService();
        orderBookService = new OrderBookService(tradeHistoryService);
        vertx.deployVerticle(new OrderApiVerticle(orderBookService), testContext.succeeding(id -> testContext.completeNow()));
    }

    @AfterEach
    public void tearDown(VertxTestContext testContext) {
        vertx.close(testContext.succeeding(ar -> testContext.completeNow()));
    }

    @Test
    public void testAddOrderAndMatchOrder() {
        // Arrange
        Order buyOrder = new Order("1", new BigDecimal("50000"), new BigDecimal("0.01"), "BTCUSD",
            Order.OrderSide.BUY, Instant.now());
        Order sellOrder = new Order("2", new BigDecimal("49900"), new BigDecimal("0.01"), "BTCUSD",
            Order.OrderSide.SELL, Instant.now());

        // Act
        orderBookService.matchOrderAndAddRemaining(buyOrder);
        orderBookService.matchOrderAndAddRemaining(sellOrder);

        // Assert
        List<Trade> trades = tradeHistoryService.getRecentTradesForPair("BTCUSD");
        assertEquals(1, trades.size());
        assertEquals(new BigDecimal("50000.0000"), trades.get(0).price());
        assertEquals(new BigDecimal("0.01000000"), trades.get(0).quantity());
    }

    @Test
    public void testPartialOrderMatch() {
        // Arrange
        Order buyOrder = new Order("3", new BigDecimal("10000"), new BigDecimal("0.02"), "BTCUSD",
            Order.OrderSide.BUY, Instant.now());
        Order sellOrder = new Order("4", new BigDecimal("10000"), new BigDecimal("0.01"), "BTCUSD",
            Order.OrderSide.SELL, Instant.now());

        // Act
        orderBookService.matchOrderAndAddRemaining(buyOrder);
        orderBookService.matchOrderAndAddRemaining(sellOrder);

        // Assert
        List<Trade> trades = tradeHistoryService.getRecentTradesForPair("BTCUSD");
        assertEquals(1, trades.size());
        assertEquals(new BigDecimal("10000.0000"), trades.get(0).price());
        assertEquals(new BigDecimal("0.01000000"), trades.get(0).quantity());

        // Verify that remaining buy order is still in the order book
        List<Order> buyOrders = orderBookService.getBuyOrdersForPair("BTCUSD");
        assertEquals(1, buyOrders.size());
        assertEquals(new BigDecimal("0.01000000"), buyOrders.get(0).getQuantity());
    }

    @Test
    public void testEmptyOrderBook() {
        // Assert
        List<Order> buyOrders = orderBookService.getBuyOrdersForPair("BTCUSD");
        List<Order> sellOrders = orderBookService.getSellOrdersForPair("BTCUSD");

        assertTrue(buyOrders.isEmpty());
        assertTrue(sellOrders.isEmpty());
    }

    @Test
    public void testAddBuyOrderWithoutMatching() {
        // Arrange
        Order buyOrder = new Order("5", new BigDecimal("15000"), new BigDecimal("0.05"), "BTCUSD", Order.OrderSide.BUY, Instant.now());

        // Act
        orderBookService.matchOrderAndAddRemaining(buyOrder);

        // Assert
        List<Order> buyOrders = orderBookService.getBuyOrdersForPair("BTCUSD");
        assertEquals(1, buyOrders.size());
        assertEquals(new BigDecimal("15000.0000"), buyOrders.get(0).getPrice());
        assertEquals(new BigDecimal("0.05000000"), buyOrders.get(0).getQuantity());
    }

    @Test
    public void testAddSellOrderWithoutMatching() {
        // Arrange
        Order sellOrder = new Order("6", new BigDecimal("20000"), new BigDecimal("0.03"), "BTCUSD", Order.OrderSide.SELL, Instant.now());

        // Act
        orderBookService.matchOrderAndAddRemaining(sellOrder);

        // Assert
        List<Order> sellOrders = orderBookService.getSellOrdersForPair("BTCUSD");
        assertEquals(1, sellOrders.size());
        assertEquals(new BigDecimal("20000.0000"), sellOrders.get(0).getPrice());
        assertEquals(new BigDecimal("0.03000000"), sellOrders.get(0).getQuantity());
    }

    @Test
    public void testOrderMatchingMultipleLevels() {
        // Arrange
        Order buyOrder1 = new Order("7", new BigDecimal("10000"), new BigDecimal("0.01"), "BTCUSD", Order.OrderSide.BUY, Instant.now());
        Order buyOrder2 = new Order("8", new BigDecimal("10100"), new BigDecimal("0.01"), "BTCUSD", Order.OrderSide.BUY, Instant.now());
        Order sellOrder = new Order("9", new BigDecimal("10000"), new BigDecimal("0.02"), "BTCUSD", Order.OrderSide.SELL, Instant.now());

        // Act
        orderBookService.matchOrderAndAddRemaining(buyOrder1);
        orderBookService.matchOrderAndAddRemaining(buyOrder2);
        orderBookService.matchOrderAndAddRemaining(sellOrder);

        // Assert
        List<Trade> trades = tradeHistoryService.getRecentTradesForPair("BTCUSD");
        assertEquals(2, trades.size());
        assertEquals(new BigDecimal("10100.0000"), trades.get(0).price());
        assertEquals(new BigDecimal("0.01000000"), trades.get(0).quantity());
        assertEquals(new BigDecimal("10000.0000"), trades.get(1).price());
        assertEquals(new BigDecimal("0.01000000"), trades.get(1).quantity());
    }
}
