package com.akaibo.orderbook;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

        client.request(HttpMethod.GET, 8090, "localhost", "/orderbook")
            .compose(request -> request.send())
            .onComplete(testContext.succeeding(response -> {
                assertEquals(200, response.statusCode());
                testContext.completeNow();
            }));
    }
}
