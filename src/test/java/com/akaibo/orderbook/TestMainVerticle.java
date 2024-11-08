package com.akaibo.orderbook;

import com.akaibo.orderbook.util.ApiConstants;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(VertxExtension.class)
public class TestMainVerticle {
    private Vertx vertx;

    @BeforeEach
    void setUp(VertxTestContext testContext) {
        vertx = Vertx.vertx();
        vertx.deployVerticle(new MainVerticle(), testContext.succeeding(id -> testContext.completeNow()));
    }

    @AfterEach
    void tearDown(VertxTestContext testContext) {
        vertx.close(testContext.succeeding(v -> testContext.completeNow()));
    }

    @Test
    void testOrderBookApiVerticle(VertxTestContext testContext) throws Throwable {
        HttpClient client = vertx.createHttpClient();

        client.request(HttpMethod.GET, 8090, "localhost", ApiConstants.ORDER_BOOK_PATH)
            .compose(request -> request.send())
            .onComplete(testContext.succeeding(response -> {
                assertEquals(200, response.statusCode());

                response.body().onComplete(testContext.succeeding(buffer -> {
                    String responseBody = buffer.toString();
                    assertTrue(responseBody.contains("bids"));
                    assertTrue(responseBody.contains("asks"));
                    testContext.completeNow();
                }));
            }));
    }

    @Test
    public void testOrderApiVerticle(VertxTestContext testContext) {
        HttpClient client = vertx.createHttpClient();
        String orderJson = "{\"side\":\"buy\",\"price\":50000,\"quantity\":0.01,\"pair\":\"ARBUSD\"}";

        client.request(HttpMethod.POST, 8091, "localhost", ApiConstants.LIMIT_ORDER_PATH)
            .compose(request -> request
                .putHeader("content-type", "application/json")
                .putHeader("content-length", String.valueOf(orderJson.length()))
                .send(Buffer.buffer(orderJson)))
            .onComplete(testContext.succeeding(response -> {
                assertEquals(200, response.statusCode());

                response.body().onComplete(testContext.succeeding(buffer -> {
                    assertEquals("Order placed successfully!", buffer.toJsonObject().getString("status"));
                    testContext.completeNow();
                }));
            }));
    }

    @Test
    public void testTradeHistoryApiVerticle(VertxTestContext testContext) {
        HttpClient client = vertx.createHttpClient();

        client.request(HttpMethod.GET, 8092, "localhost", ApiConstants.TRADE_HISTORY_PATH)
            .compose(request -> request.send())
            .onComplete(testContext.succeeding(response -> {
                assertEquals(200, response.statusCode());

                response.body().onComplete(testContext.succeeding(buffer -> {
                    assertTrue(buffer.toJsonArray().size() >= 0);
                    testContext.completeNow();
                }));
            }));
    }
}
