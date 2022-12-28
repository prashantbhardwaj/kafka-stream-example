package com.appcloid.kafka.stream.example;

import com.appcloid.kafka.stream.example.model.*;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

public class Constants {
    public static final String PRODUCT_AGGREGATE_STATE_STORE = "product_aggregate_state";
    public static final Serde<Delivery> DELIVERY_SERDE = new JsonSerde<>(Delivery.class);
    public static final Serde<Address> ADDRESS_SERDE = new JsonSerde<>(Address.class);
    public static final Serde<Person> PERSON_SERDE = new JsonSerde<>(Person.class);
    public static final Serde<Order> ORDER_SERDE = new JsonSerde<>(Order.class);
    public static final Serde<Product> PRODUCT_SERDE = new JsonSerde<>(Product.class);
    public static final Serde<ItemAddedInCart> ITEM_SERDE = new JsonSerde<>(ItemAddedInCart.class);
    public static final Serde<String> KEY_SERDE = Serdes.String();
}
