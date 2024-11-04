package com.akaibo.orderbook;

import com.akaibo.orderbook.api.OrderBookApiVerticle;
import com.akaibo.orderbook.api.TradeHistoryApiVerticle;
import com.akaibo.orderbook.service.OrderBookService;
import io.vertx.core.AbstractVerticle;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start() {
        OrderBookService orderBookService = new OrderBookService();

        vertx.deployVerticle(new OrderBookApiVerticle(orderBookService));
        vertx.deployVerticle(new TradeHistoryApiVerticle(orderBookService));
    }
}
