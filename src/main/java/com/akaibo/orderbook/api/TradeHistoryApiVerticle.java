package com.akaibo.orderbook.api;

import com.akaibo.orderbook.service.OrderBookService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

public class TradeHistoryApiVerticle extends AbstractVerticle {
    private final OrderBookService orderBookService;

    public TradeHistoryApiVerticle(OrderBookService orderBookService) {
        this.orderBookService = orderBookService;
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {

        Router router = Router.router(vertx);

        // Endpoint to get recent trades
        router.get("/v1/:pair/tradehistory").handler(ctx -> {
            String currencyParam = ctx.pathParam("pair").toUpperCase();
            JsonArray tradesArray = new JsonArray();

            orderBookService.getRecentTradesForPair(currencyParam).forEach(trade -> {
                tradesArray.add(new JsonObject()
                    .put("id", trade.id())
                    .put("price", trade.price().toString())
                    .put("quantity", trade.quantity().toString())
                    .put("currencyPair", trade.currencyPair())
                    .put("tradedAt", trade.tradedAt())
                    .put("takerSide", trade.takerSide())
                    .put("quoteVolume", trade.quoteVolume().toString()));
            });
            ctx.response()
                .putHeader("content-type", "application/json")
                .end(tradesArray.encodePrettily());
        });

        // Start the HTTP server on port 8090
        vertx.createHttpServer().requestHandler(router).listen(8092).onComplete(http -> {
            if (http.succeeded()) {
                startPromise.complete();
                System.out.println("HTTP server started on port 8092");
            } else {
                startPromise.fail(http.cause());
            }
        });
    }

}
