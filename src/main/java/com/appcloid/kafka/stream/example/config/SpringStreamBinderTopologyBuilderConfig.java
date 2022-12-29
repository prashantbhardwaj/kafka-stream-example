package com.appcloid.kafka.stream.example.config;

import com.appcloid.kafka.stream.example.Constants;
import com.appcloid.kafka.stream.example.model.ItemAddedInCart;
import com.appcloid.kafka.stream.example.model.Order;
import com.appcloid.kafka.stream.example.model.Product;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.appcloid.kafka.stream.example.Constants.PRODUCT_AGGREGATE_STATE_STORE;

@Configuration
public class SpringStreamBinderTopologyBuilderConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpringStreamBinderTopologyBuilderConfig.class);

    @Bean
    public Function<KStream<String, Product>, KTable<String, Product>> aggregateProducts() {
        // Spring module for kafka stream has JsonSerde, while we are using our own
        ObjectMapper mapper = new ObjectMapper();
        Serde<Product> productEventSerde = new org.springframework.kafka.support.serializer.JsonSerde<>( Product.class, mapper );

        return input -> input
                .peek((k,v) -> LOGGER.info("Received product with key [{}] and value [{}]",k, v))
                .groupByKey()
                .aggregate(Product::new,
                        (key, value, aggregate) -> aggregate.process(value),
                        Materialized.<String, Product, KeyValueStore<Bytes, byte[]>>as(PRODUCT_AGGREGATE_STATE_STORE).withValueSerde(productEventSerde)//.withKeySerde(keySerde)
                        // because keySerde is configured in application.properties
                );
    }

    @Bean
    public BiConsumer<KStream<String, ItemAddedInCart>, KTable<String, Product>> processCartItem(){
        return (cartItem, aggregatedProduct) -> cartItem
                .peek((k, v) -> LOGGER.info("Item in cart received with key [{}] and value [{}]", k, v))
                .leftJoin(
                        aggregatedProduct,
                        (item, product) -> product != null ? product.checkIfOrderQuantityAvailable(item) : Order.builder().id(UUID.randomUUID().hashCode()).state(Order.OrderState.REJECTED_PRODUCT_NOT_FOUND).build(),
                        Joined.with(Constants.KEY_SERDE, Constants.ITEM_SERDE, Constants.PRODUCT_SERDE)
                )
                .peek((k,v) -> LOGGER.info("for item key [{}], created order [{}]", k, v))
                .filter((k, o) -> o.getState().isRejected())
                .to(Constants.OUT_REJECTED_ORDERS, Produced.with(Constants.KEY_SERDE, Constants.ORDER_SERDE));
    }
}
