package com.akaibo.orderbook.api;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class OrderBookApiVerticle extends AbstractVerticle {

    private final List<JsonObject> buyOrders = new ArrayList<>();
    private final List<JsonObject> sellOrders = new ArrayList<>();

    @Override
    public void start(Promise<Void> startPromise) throws Exception {

        // Initialize the order book with some sample data
        initializeOrderBook();

        // Create a Router for handling HTTP requests
        Router router = Router.router(vertx);

        // Define the endpoint to get the order book
        router.get("/orderbook").handler(ctx -> {
            JsonObject response = new JsonObject()
                .put("asks", new JsonArray(sellOrders))
                .put("bids", new JsonArray(buyOrders));

            ctx.response()
                .putHeader("content-type", "application/json")
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

    private void initializeOrderBook() {
        // Add some sample buy orders (bids)
        buyOrders.add(new JsonObject().put("price", "900").put("quantity", "1"));
        buyOrders.add(new JsonObject().put("price", "850").put("quantity", "0.5"));
        buyOrders.add(new JsonObject().put("price", "800").put("quantity", "2"));

        // Add some sample sell orders (asks)
        sellOrders.add(new JsonObject().put("price", "1000").put("quantity", "1"));
        sellOrders.add(new JsonObject().put("price", "1050").put("quantity", "0.75"));
        sellOrders.add(new JsonObject().put("price", "1100").put("quantity", "1.5"));

        // Sort buy orders by price descending
        buyOrders.sort(Comparator.comparing(o -> o.getString("price"), Comparator.reverseOrder()));

        // Sort sell orders buy price ascending
        sellOrders.sort(Comparator.comparing(o -> o.getString("price")));
    }
}
