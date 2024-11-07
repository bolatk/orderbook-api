package com.akaibo.orderbook.api;

import com.akaibo.orderbook.model.Order;
import com.akaibo.orderbook.service.OrderBookService;
import com.akaibo.orderbook.util.ApiConstants;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

import java.math.BigDecimal;
import java.time.Instant;

public class OrderApiVerticle extends AbstractVerticle {

    private final OrderBookService orderBookService;

    public OrderApiVerticle(OrderBookService orderBookService) {
        this.orderBookService = orderBookService;
    }

    @Override
    public void start(Promise<Void> startPromise) {
        Router router = Router.router(vertx);

        // Endpoint to submit a new limit order
        router.post(ApiConstants.LIMIT_ORDER_PATH).handler(ctx -> ctx.request().bodyHandler(buffer -> {
            if (buffer == null || buffer.length() == 0) {
                JsonObject response = new JsonObject()
                    .put(ApiConstants.ERROR, "Request body is empty");
                ctx.response()
                    .setStatusCode(400)
                    .putHeader(ApiConstants.CONTENT_TYPE, ApiConstants.CONTENT_TYPE_JSON)
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
                    .put(ApiConstants.ERROR, ApiConstants.INVALID_ORDER_SIDE_ERROR);
                ctx.response()
                    .setStatusCode(400)
                    .putHeader(ApiConstants.CONTENT_TYPE, ApiConstants.CONTENT_TYPE_JSON)
                    .end(response.encodePrettily());
                return;
            }
            // Extracting price and quantity as BigDecimal
            String priceStr = body.getString("price", null);
            String quantityStr = body.getString("quantity", null);
            if (priceStr == null || quantityStr == null) {
                JsonObject response = new JsonObject()
                    .put(ApiConstants.ERROR, "Missing required fields 'price' or 'quantity'.");
                ctx.response()
                    .setStatusCode(400)
                    .putHeader(ApiConstants.CONTENT_TYPE, ApiConstants.CONTENT_TYPE_JSON)
                    .end(response.encodePrettily());
                return;
            }
            BigDecimal price = BigDecimal.valueOf(body.getDouble("price"));
            BigDecimal quantity = BigDecimal.valueOf(body.getDouble("quantity"));
            String pair = body.getString("pair");
            Order order = new Order(java.util.UUID.randomUUID().toString(), price, quantity, pair, side, Instant.now());

            orderBookService.matchOrderAndAddRemaining(order);

            JsonObject response = new JsonObject()
                .put("status", ApiConstants.ORDER_PLACED_SUCCESS);
            ctx.response()
                .putHeader(ApiConstants.CONTENT_TYPE, ApiConstants.CONTENT_TYPE_JSON)
                .end(response.encodePrettily());
        }));

        // Start the HTTP server on port 8091
        vertx.createHttpServer().requestHandler(router).listen(8091).onComplete(http -> {
            if (http.succeeded()) {
                startPromise.complete();
                System.out.println("HTTP server started on port 8091");
            } else {
                startPromise.fail(http.cause());
            }
        });
    }
}
