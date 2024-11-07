package com.akaibo.orderbook.api;

import com.akaibo.orderbook.service.TradeHistoryService;
import com.akaibo.orderbook.util.ApiConstants;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

public class TradeHistoryApiVerticle extends AbstractVerticle {
    private final TradeHistoryService tradeHistoryService;

    public TradeHistoryApiVerticle(TradeHistoryService tradeHistoryService) {
        this.tradeHistoryService = tradeHistoryService;
    }

    @Override
    public void start(Promise<Void> startPromise) {
        Router router = Router.router(vertx);

        // Endpoint to get recent trades
        router.get(ApiConstants.TRADE_HISTORY_PATH).handler(ctx -> {
            String currencyParam = ctx.pathParam("pair").toUpperCase();
            JsonArray tradesArray = new JsonArray();

            tradeHistoryService.getRecentTradesForPair(currencyParam).forEach(trade -> tradesArray.add(new JsonObject()
                .put(ApiConstants.PRICE_PARAM, trade.price().stripTrailingZeros().toPlainString())
                .put(ApiConstants.QUANTITY_PARAM, trade.quantity().stripTrailingZeros().toPlainString())
                .put(ApiConstants.CURRENCY_PAIR, trade.currencyPair())
                .put("tradedAt", trade.tradedAt())
                .put("takerSide", trade.takerSide())
                .put("id", trade.id())
                .put("quoteVolume", trade.quoteVolume().stripTrailingZeros().toPlainString())));
            ctx.response()
                .putHeader(ApiConstants.CONTENT_TYPE, ApiConstants.CONTENT_TYPE_JSON)
                .end(tradesArray.encodePrettily());
        });

        // Start the HTTP server on port 8092
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
