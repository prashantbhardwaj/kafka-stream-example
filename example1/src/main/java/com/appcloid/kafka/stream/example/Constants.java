package com.appcloid.kafka.stream.example;

import com.appcloid.kafka.stream.example.model.entities.*;
import com.appcloid.kafka.stream.example.model.events.ItemAddedInCart;
import com.appcloid.kafka.stream.example.model.events.ProductUpdated;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.springframework.kafka.support.serializer.JsonSerde;

/**
 * Class to hold constants for this application
 */
public class Constants {
    // state stores
    public static final String PRODUCT_AGGREGATE_STATE_STORE = "product_aggregate_state_store";
    public static final String PRODUCT_UPDATE_STATE_STORE = "product_update_state_store";
    public static final String ALL_CART_ITEM_PRODUCT_JOIN_STORE = "all_cart_items_product_join_store";
    public static final String ORDER_STATE_STORE = "order_state_store";
    public static final String CUSTOMER_CART_STATE_STORE = "order_state_store";

    // topics
    public static final String OUT_REJECTED_CART_ITEMS_TOPIC = "rejected_cart_items";
    public static final String OUT_APPROVED_CART_ITEMS_TOPIC = "approved_cart_items";
    public static final String OUT_REJECTED_ORDERS_TOPIC = "rejected_orders";
    public static final String OUT_APPROVED_ORDERS_TOPIC = "approved_orders";

    // Serdes
    public static final Serde<Delivery> DELIVERY_SERDE = new JsonSerde<>(Delivery.class);
    public static final Serde<Address> ADDRESS_SERDE = new JsonSerde<>(Address.class);
    public static final Serde<Person> PERSON_SERDE = new JsonSerde<>(Person.class);
    public static final Serde<CustomerCart> CUSTOMER_CART_SERDE = new JsonSerde<>(CustomerCart.class);
    public static final Serde<Order> ORDER_SERDE = new JsonSerde<>(Order.class);
    public static final Serde<Product> PRODUCT_SERDE = new JsonSerde<>(Product.class);
    public static final Serde<ProductUpdated> PRODUCT_UPDATE_SERDE = new JsonSerde<>(ProductUpdated.class);
    public static final Serde<ItemAddedInCart> ITEM_SERDE = new JsonSerde<>(ItemAddedInCart.class);
    public static final Serde<String> KEY_SERDE = Serdes.String();



}
