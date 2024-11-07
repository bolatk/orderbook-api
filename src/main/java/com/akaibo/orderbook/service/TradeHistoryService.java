package com.akaibo.orderbook.service;

import com.akaibo.orderbook.model.Trade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TradeHistoryService {
    private final Map<String, List<Trade>> recentTrades;

    public TradeHistoryService() {
        recentTrades = new HashMap<>();
    }

    public void addTrade(Trade trade) {
        recentTrades.computeIfAbsent(trade.currencyPair(), k -> new ArrayList<>()).add(trade);
    }

    public List<Trade> getRecentTradesForPair(String currencyPair) {
        return recentTrades.getOrDefault(currencyPair, new ArrayList<>()).stream()
            .sorted((trade1, trade2) -> trade2.tradedAt().compareTo(trade1.tradedAt())) // Sort by tradedAt descending
            .toList();
    }
}
