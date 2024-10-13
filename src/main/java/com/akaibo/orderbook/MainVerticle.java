package com.akaibo.orderbook;

import com.akaibo.orderbook.api.OrderBookApiVerticle;
import io.vertx.core.AbstractVerticle;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start() {
        vertx.deployVerticle(new OrderBookApiVerticle());
    }
}
