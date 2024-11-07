package com.akaibo.orderbook.api;

import com.akaibo.orderbook.model.Order;
import com.akaibo.orderbook.service.OrderBookService;
import com.akaibo.orderbook.util.ApiConstants;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OrderBookApiVerticle extends AbstractVerticle {
    private final OrderBookService orderBookService;

    public OrderBookApiVerticle(OrderBookService orderBookService) {
        this.orderBookService = orderBookService;
    }

    @Override
    public void start(Promise<Void> startPromise) {
        // Create a Router for handling HTTP requests
        Router router = Router.router(vertx);

        // Endpoint to get the order book for a specific currency pair
        router.get(ApiConstants.ORDER_BOOK_PATH).handler(ctx -> {
            String currencyPair = ctx.pathParam(ApiConstants.CURRENCY_PAIR_PARAM).toUpperCase();
            JsonObject response = new JsonObject();
            JsonArray buyOrders = new JsonArray();
            JsonArray sellOrders = new JsonArray();

            Map<BigDecimal, List<Order>> aggregatedBuyOrders = orderBookService.getBuyOrdersForPair(currencyPair)
                .stream()
                .collect(Collectors.groupingBy(Order::getPrice));

            aggregatedBuyOrders.forEach((price, orders) -> {
                BigDecimal totalQuantity = orders.stream()
                    .map(Order::getQuantity)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                int orderCount = orders.size();

                buyOrders.add(new JsonObject()
                    .put(ApiConstants.SIDE_PARAM, "buy")
                    .put(ApiConstants.QUANTITY_PARAM, totalQuantity.stripTrailingZeros().toPlainString())
                    .put(ApiConstants.PRICE_PARAM, price.stripTrailingZeros().toPlainString())
                    .put(ApiConstants.CURRENCY_PAIR, currencyPair)
                    .put(ApiConstants.ORDER_COUNT, orderCount));
            });

            // Aggregate sell orders by price
            Map<BigDecimal, List<Order>> aggregatedSellOrders = orderBookService.getSellOrdersForPair(currencyPair)
                .stream()
                .collect(Collectors.groupingBy(Order::getPrice));

            aggregatedSellOrders.forEach((price, orders) -> {
                BigDecimal totalQuantity = orders.stream()
                    .map(Order::getQuantity)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                int orderCount = orders.size();

                sellOrders.add(new JsonObject()
                    .put(ApiConstants.SIDE_PARAM, "sell")
                    .put(ApiConstants.QUANTITY_PARAM, totalQuantity.stripTrailingZeros().toPlainString())
                    .put(ApiConstants.PRICE_PARAM, price.stripTrailingZeros().toPlainString())
                    .put(ApiConstants.CURRENCY_PAIR, currencyPair)
                    .put(ApiConstants.ORDER_COUNT, orderCount));
            });

            response.put("bids", buyOrders)
                    .put("asks", sellOrders);

            ctx.response()
                .putHeader(ApiConstants.CONTENT_TYPE, ApiConstants.CONTENT_TYPE_JSON)
                .end(response.encodePrettily());
        });

        // Start the HTTP server on port 8090
        vertx.createHttpServer().requestHandler(router).listen(8090).onComplete(http -> {
            if (http.succeeded()) {
                startPromise.complete();
                System.out.println("HTTP server started on port 8090");
            } else {
                startPromise.fail(http.cause());
            }
        });
    }
}
