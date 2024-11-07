package com.akaibo.orderbook.service;

import com.akaibo.orderbook.model.Order;
import com.akaibo.orderbook.model.Trade;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;

public class OrderBookService {
    // Buy orders sorted in descending order (the highest price first)
    private final NavigableMap<BigDecimal, LinkedList<Order>> buyOrders;
    // Sell orders sorted in ascending order (the lowest price first)
    private final NavigableMap<BigDecimal, LinkedList<Order>> sellOrders;
    private final TradeHistoryService tradeHistoryService;

    public OrderBookService(TradeHistoryService tradeHistoryService) {
        buyOrders = new TreeMap<>(Comparator.reverseOrder());
        sellOrders = new TreeMap<>();
        this.tradeHistoryService = tradeHistoryService;
    }

    // Method to match an order with the opposite side of the order book
    // Partial Matching is supported: If the incoming order is larger than the best available order on the other side,
    // the incoming order will continue to match until it's either filled or no matching orders remain.
    public void matchOrderAndAddRemaining(Order newOrder) {
        if (newOrder.getSide().equals(Order.OrderSide.BUY)) {
            matchWithSellOrders(newOrder);
        } else if (newOrder.getSide().equals(Order.OrderSide.SELL)) {
            matchWithBuyOrders(newOrder);
        }
        // Add remaining portion to the order book if it wasn't fully matched
        if (newOrder.getQuantity().compareTo(BigDecimal.ZERO) > 0) {
            addNewOrderToOrderBook(newOrder);
        }
    }

    // Helper method to match with sell orders
    private void matchWithSellOrders(Order buyOrder) {
        while (!sellOrders.isEmpty() && buyOrder.getQuantity().compareTo(BigDecimal.ZERO) > 0) {
            Map.Entry<BigDecimal, LinkedList<Order>> entry = sellOrders.firstEntry();
            if (buyOrder.getPrice().compareTo(entry.getKey()) >= 0) {
                LinkedList<Order> sellList = entry.getValue();
                while (!sellList.isEmpty() && buyOrder.getQuantity().compareTo(BigDecimal.ZERO) > 0) {
                    Order sellOrder = sellList.peek();
                    BigDecimal matchedQuantity = buyOrder.getQuantity()
                        .min(Objects.requireNonNull(sellOrder, "sellOrder is NULL").getQuantity());

                    // Update quantities
                    buyOrder.setQuantity(buyOrder.getQuantity().subtract(matchedQuantity));
                    sellOrder.setQuantity(sellOrder.getQuantity().subtract(matchedQuantity));

                    // Quote volume represents the total value of the trade in the quote currency (usually the currency against which an asset is being traded).
                    BigDecimal quoteVolume = sellOrder.getPrice().multiply(matchedQuantity);

                    System.out.println("Matched: " + matchedQuantity.stripTrailingZeros().toPlainString() +
                        " at price: " + sellOrder.getPrice().stripTrailingZeros().toPlainString());

                    // Record trade
                    Trade trade = new Trade(java.util.UUID.randomUUID().toString(), sellOrder.getPrice(), matchedQuantity,
                        sellOrder.getPair(), Instant.now(), buyOrder.getSide().name(), quoteVolume);
                    tradeHistoryService.addTrade(trade);

                    // Remove the sell order if fully matched
                    if (sellOrder.getQuantity().compareTo(BigDecimal.ZERO) == 0) {
                        sellList.poll();
                    }

                    // Remove the price level if no orders are left
                    if (sellList.isEmpty()) {
                        sellOrders.remove(entry.getKey());
                    }
                }
            } else {
                break; // No more sell orders that can match the buy order's price
            }
        }
    }

    // Helper method to match with buy orders
    private void matchWithBuyOrders(Order sellOrder) {
        while (!buyOrders.isEmpty() && sellOrder.getQuantity().compareTo(BigDecimal.ZERO) > 0) {
            Map.Entry<BigDecimal, LinkedList<Order>> entry = buyOrders.firstEntry();
            if (sellOrder.getPrice().compareTo(entry.getKey()) <= 0) {
                LinkedList<Order> buyList = entry.getValue();
                while (!buyList.isEmpty() && sellOrder.getQuantity().compareTo(BigDecimal.ZERO) > 0) {
                    Order buyOrder = buyList.peek();
                    BigDecimal matchedQuantity = sellOrder.getQuantity()
                        .min(Objects.requireNonNull(buyOrder, "buyOrder is NULL").getQuantity());

                    // Update quantities
                    sellOrder.setQuantity(sellOrder.getQuantity().subtract(matchedQuantity));
                    buyOrder.setQuantity(buyOrder.getQuantity().subtract(matchedQuantity));

                    // Quote volume represents the total value of the trade in the quote currency (usually the currency against which an asset is being traded).
                    BigDecimal quoteVolume = buyOrder.getPrice().multiply(matchedQuantity);

                    System.out.println("Matched: " + matchedQuantity.stripTrailingZeros().toPlainString() +
                        " at price: " + buyOrder.getPrice().stripTrailingZeros().toPlainString());

                    Trade trade = new Trade(java.util.UUID.randomUUID().toString(), buyOrder.getPrice(), matchedQuantity,
                        buyOrder.getPair(), Instant.now(), sellOrder.getSide().name(), quoteVolume);
                    tradeHistoryService.addTrade(trade);

                    // Remove the buy order if fully matched
                    if (buyOrder.getQuantity().compareTo(BigDecimal.ZERO) == 0) {
                        buyList.poll();
                    }

                    // Remove the price level if no orders are left
                    if (buyList.isEmpty()) {
                        buyOrders.remove(entry.getKey());
                    }
                }
            } else {
                break; // No more buy orders that can match the sell order's price
            }
        }
    }

    // Method to add remaining order to the appropriate order book
    public void addNewOrderToOrderBook(Order order) {
        if (order.getSide().equals(Order.OrderSide.BUY)) {
            // buyOrders map is organized by price levels
            buyOrders.computeIfAbsent(order.getPrice(), k -> new LinkedList<>()).add(order);
        } else if (order.getSide().equals(Order.OrderSide.SELL)) {
            // sellOrders map is organized by price levels
            sellOrders.computeIfAbsent(order.getPrice(), k -> new LinkedList<>()).add(order);
        }
    }

    // Get buy orders for a specific currency pair
    public List<Order> getBuyOrdersForPair(String currencyPair) {
        return buyOrders.values().stream()
            .flatMap(List::stream)
            .filter(order -> order.getPair().equals(currencyPair))
            .toList();
    }
    // Get sell orders for a specific currency pair
    public List<Order> getSellOrdersForPair(String currencyPair) {
        return sellOrders.values().stream()
            .flatMap(List::stream)
            .filter(order -> order.getPair().equals(currencyPair))
            .toList();
    }
}
