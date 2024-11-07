package com.akaibo.orderbook;

import com.akaibo.orderbook.api.OrderApiVerticle;
import com.akaibo.orderbook.api.OrderBookApiVerticle;
import com.akaibo.orderbook.api.TradeHistoryApiVerticle;
import com.akaibo.orderbook.service.OrderBookService;
import com.akaibo.orderbook.service.TradeHistoryService;
import io.vertx.core.AbstractVerticle;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start() {
        TradeHistoryService tradeHistoryService = new TradeHistoryService();
        OrderBookService orderBookService = new OrderBookService(tradeHistoryService);

        vertx.deployVerticle(new OrderBookApiVerticle(orderBookService));
        vertx.deployVerticle(new OrderApiVerticle(orderBookService));
        vertx.deployVerticle(new TradeHistoryApiVerticle(tradeHistoryService));
    }
}
