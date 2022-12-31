package com.appcloid.kafka.stream.example;

import com.appcloid.kafka.stream.example.model.*;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

/**
 * Class to hold constants for this application
 */
public class Constants {
    // state stores
    public static final String PRODUCT_AGGREGATE_STATE_STORE = "product_aggregate_state_store";
    public static final String ORDER_STATE_STORE = "order_state_store";

    // topics
    public static final String OUT_REJECTED_ORDERS_TOPIC = "rejected_orders";
    public static final String OUT_APPROVED_ORDERS_TOPIC = "approved_orders";

    // Serdes
    public static final Serde<Delivery> DELIVERY_SERDE = new JsonSerde<>(Delivery.class);
    public static final Serde<Address> ADDRESS_SERDE = new JsonSerde<>(Address.class);
    public static final Serde<Person> PERSON_SERDE = new JsonSerde<>(Person.class);
    public static final Serde<Order> ORDER_SERDE = new JsonSerde<>(Order.class);
    public static final Serde<Product> PRODUCT_SERDE = new JsonSerde<>(Product.class);
    public static final Serde<CartItem> ITEM_SERDE = new JsonSerde<>(CartItem.class);
    public static final Serde<String> KEY_SERDE = Serdes.String();


}
