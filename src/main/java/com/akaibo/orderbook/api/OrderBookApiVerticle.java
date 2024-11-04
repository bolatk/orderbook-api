package com.akaibo.orderbook.api;

import com.akaibo.orderbook.model.Order;
import com.akaibo.orderbook.model.Trade;
import com.akaibo.orderbook.service.OrderBookService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class OrderBookApiVerticle extends AbstractVerticle {

    // A map to hold order books for different currency pairs
    // private final Map<String, OrderBook> orderBooks = new ConcurrentHashMap<>();
    private final Map<Double, PriorityQueue<Order>> buyOrders = new ConcurrentHashMap<>();
    private final Map<Double, PriorityQueue<Order>> sellOrders = new ConcurrentHashMap<>();
    private final Map<String, Trade> tradeHistory = new ConcurrentHashMap<>();
    private final OrderBookService orderBookService;

    public OrderBookApiVerticle(OrderBookService orderBookService) {
        this.orderBookService = orderBookService;
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        // Create a Router for handling HTTP requests
        Router router = Router.router(vertx);

        // Endpoint to get the order book for a specific currency pair
        router.get("/v1/:pair/orderbook").handler(ctx -> {
            String currencyPair = ctx.pathParam("pair").toUpperCase();
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
                    .put("side", "buy")
                    .put("quantity", totalQuantity.toString())
                    .put("price", price.toString())
                    .put("currencyPair", currencyPair)
                    .put("orderCount", orderCount));
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
                    .put("side", "sell")
                    .put("quantity", totalQuantity.toString())
                    .put("price", price.toString())
                    .put("currencyPair", currencyPair)
                    .put("orderCount", orderCount));
            });

            response.put("bids", buyOrders)
                    .put("asks", sellOrders);

            ctx.response()
                .putHeader("content-type", "application/json")
                .end(response.encodePrettily());
        });

        // Endpoint to submit a new limit order
        router.post("/v1/orders/limit").handler(ctx -> {
            ctx.request().bodyHandler(buffer -> {
                if (buffer == null || buffer.length() == 0) {
                    JsonObject response = new JsonObject()
                        .put("error", "Request body is empty");
                    ctx.response()
                        .setStatusCode(400)
                        .putHeader("content-type", "application/json")
                        .end(response.encodePrettily());
                    return;
                }
                JsonObject body = buffer.toJsonObject();
                String sideString = body.getString("side").toUpperCase(); // Convert side to uppercase to match enum values

                Order.OrderSide side;
                try {
                    side = Order.OrderSide.valueOf(sideString);
                } catch (IllegalArgumentException e) {
                    JsonObject response = new JsonObject()
                        .put("error", "Invalid order side. Must be 'buy' or 'sell'.");
                    ctx.response()
                        .setStatusCode(400)
                        .putHeader("content-type", "application/json")
                        .end(response.encodePrettily());
                    return;
                }
                // Extracting price and quantity as BigDecimal
                String priceStr = body.getString("price", null);
                String quantityStr = body.getString("quantity", null);
                if (priceStr == null || quantityStr == null) {
                    JsonObject response = new JsonObject()
                        .put("error", "Missing required fields 'price' or 'quantity'.");
                    ctx.response()
                        .setStatusCode(400)
                        .putHeader("content-type", "application/json")
                        .end(response.encodePrettily());
                    return;
                }
                BigDecimal price = new BigDecimal(body.getString("price"));
                BigDecimal quantity = new BigDecimal(body.getDouble("quantity"));
                String pair = body.getString("pair");
                Order order = new Order(java.util.UUID.randomUUID().toString(), price, quantity, pair, side, Instant.now());

                orderBookService.addOrderToBook(order);
                orderBookService.matchOrderAndAddRemaining(order);

                JsonObject response = new JsonObject()
                    .put("status", "Order placed successfully!");
                ctx.response()
                    .putHeader("content-type", "application/json")
                    .end(response.encodePrettily());
            });
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
