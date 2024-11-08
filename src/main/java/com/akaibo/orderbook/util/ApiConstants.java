package com.akaibo.orderbook.util;

public final class ApiConstants {
    // API Version
    public static final String API_VERSION = "/api/v1";

    // Endpoint Paths
    public static final String ORDER_BOOK_PATH = API_VERSION + "/:pair/orderbook";
    public static final String LIMIT_ORDER_PATH = API_VERSION + "/orders/limit";
    public static final String TRADE_HISTORY_PATH = API_VERSION + "/:pair/tradehistory";

    // Parameter Names
    public static final String CURRENCY_PAIR_PARAM = "pair";
    public static final String SIDE_PARAM = "side";
    public static final String PRICE_PARAM = "price";
    public static final String QUANTITY_PARAM = "quantity";
    public static final String CURRENCY_PAIR = "currencyPair";
    public static final String ORDER_COUNT = "orderCount";

    // General Headers
    public static final String CONTENT_TYPE = "content-type";
    public static final String CONTENT_TYPE_JSON = "application/json";

    // Error Messages
    public static final String INVALID_ORDER_SIDE_ERROR = "Invalid order side. Must be 'buy' or 'sell'.";

    // Response Messages
    public static final String ORDER_PLACED_SUCCESS = "Order placed successfully!";
    public static final String ERROR = "error";

    private ApiConstants() {
        // Prevent instantiation
    }
}
