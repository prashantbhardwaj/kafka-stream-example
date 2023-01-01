package com.appcloid.kafka.stream.example.config;

import com.appcloid.kafka.stream.example.Constants;
import com.appcloid.kafka.stream.example.model.CartItem;
import com.appcloid.kafka.stream.example.model.Order;
import com.appcloid.kafka.stream.example.model.Person;
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
import org.springframework.kafka.support.serializer.JsonSerde;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.appcloid.kafka.stream.example.Constants.PRODUCT_AGGREGATE_STATE_STORE;
import static com.appcloid.kafka.stream.example.Constants.PRODUCT_UPDATE_STATE_STORE;

@Configuration
public class SpringStreamBinderTopologyBuilderConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpringStreamBinderTopologyBuilderConfig.class);

    /**
     * This stream processing acts on products and aggregate them on product_id which is coming as key of the message.
     * Product with aggregated quantity is then held in state_store, which is kept in a Kafka topic.
     * @return
     */
    @Bean
    public Function<KStream<String, Product>, KTable<String, Product>> aggregateProducts() {
        // Spring module for kafka stream has JsonSerde, while we are using our own
        ObjectMapper mapper = new ObjectMapper();
        Serde<Product> productEventSerde = new JsonSerde<>( Product.class, mapper );

        return input -> input
                .peek((k,v) -> LOGGER.info("Received product with key [{}] and value [{}]",k, v))
                .groupByKey()
                .aggregate(Product::new,
                        (key, value, aggregate) -> aggregate.process(value),
                        Materialized.<String, Product, KeyValueStore<Bytes, byte[]>>as(PRODUCT_AGGREGATE_STATE_STORE).withValueSerde(productEventSerde)//.withKeySerde(keySerde)
                        // because keySerde is configured in application.properties
                );
    }

    /**
     * This stream processing acts on the cart item events which are getting generated when user is adding items in the cart;
     * Stream is then joined with the aggregated_product state to check whether the item quantity selected by the user is available or not
     * Rejected Items are sent on a kafka topic (OUT_REJECTED_ORDERS_TOPIC);
     * Items can be rejected because either selected product is not stored in the state or selected quantity is not available.
     * Approved Items are sent on a kafka topic (OUT_APPROVED_ORDERS_TOPIC);
     * @return
     */
    @Bean
    public BiFunction<KStream<String, CartItem>, KTable<String, Product>, KStream<String, CartItem>[]> processCartItem(){
        return (cartItem, aggregatedProduct) -> cartItem
                .peek((k, v) -> LOGGER.info("Item in cart received with key [{}] and value [{}]", k, v))
                .leftJoin(
                        aggregatedProduct,
                        (i, p) -> p != null ? p.checkIfOrderQuantityAvailable(i) : CartItem.builder().productId(i.getProductId()).quantity(i.getQuantity()).state(CartItem.CartItemState.REJECTED_PRODUCT_NOT_FOUND).build(),
                        Joined.with(Constants.KEY_SERDE, Constants.ITEM_SERDE, Constants.PRODUCT_SERDE).withName(Constants.ALL_CART_ITEM_PRODUCT_JOIN_STORE)
                )
                .peek((k,v) -> LOGGER.info("for item key [{}], created order [{}]", k, v))
                .split()
                .branch((k, o) -> o.getState().isRejected(), Branched.<String, CartItem>withConsumer(str -> str.to(Constants.OUT_REJECTED_CART_ITEMS_TOPIC, Produced.with(Constants.KEY_SERDE, Constants.ITEM_SERDE))).withName(Constants.OUT_REJECTED_CART_ITEMS_TOPIC))
                .defaultBranch(Branched.<String, CartItem>withConsumer(str -> str.to(Constants.OUT_APPROVED_CART_ITEMS_TOPIC, Produced.with(Constants.KEY_SERDE, Constants.ITEM_SERDE))).withName(Constants.OUT_APPROVED_CART_ITEMS_TOPIC))
                .values()
                .toArray(new KStream[0]); // if you are returning a BiFunction of <KStream<String, ItemAddedInCart>, KTable<String, Product>, KStream<String, Order>>
    }

    /**
     * Now the product quantity as ordered by the user should be deducted from the product state
     * @return
     */
    @Bean
    public Function<KStream<String, CartItem>, KStream<String, Order>> aggregateCartItemsByUserId(){
        return approvedCartItems -> approvedCartItems
                .peek((k,v) -> LOGGER.info("processing approved cart items with key [{}], and value [{}]", k, v))
                .filter((k, cartItem) -> !cartItem.getState().isRejected() && cartItem.getUserId() != 0)
                .groupBy((k, o) -> String.valueOf(o.getUserId()))
                .aggregate(
                        Order::new,
                        (key, value, aggregate) -> aggregate.addCartItem(value),
                        Materialized.<String, Order, KeyValueStore<Bytes, byte[]>>as(Constants.ORDER_STATE_STORE).withValueSerde(Constants.ORDER_SERDE)
                )
                .toStream((k, v) -> String.valueOf(v.getId()))
                .peek((k,v) -> LOGGER.info("order aggregated with key [{}], and value [{}]", k, v));
    }

    /**
     * Now the product quantity as ordered by the user should be deducted from the product state
     * @return
     */
    @Bean
    public BiFunction<KStream<String, CartItem>, KTable<String, Product>, KTable<String, Product>> deductItemQuantityFromProductState(){
        return (approvedItem, aggregatedProduct) -> approvedItem
                .peek((k,v) -> LOGGER.info("processing approved cart item with key [{}], and value [{}]", k, v))
                .leftJoin(
                        aggregatedProduct,
                        (item, product) -> product.deductCartItemQuantity(item),
                        Joined.with(Constants.KEY_SERDE, Constants.ITEM_SERDE, Constants.PRODUCT_SERDE, Constants.PRODUCT_UPDATE_STATE_STORE)
                )
                .peek((k,v) -> LOGGER.info("after processing cart quantity updated on product with key [{}], and value [{}]", k, v))
                .toTable(Materialized.<String, Product, KeyValueStore<Bytes, byte[]>>as(PRODUCT_UPDATE_STATE_STORE).withValueSerde(Constants.PRODUCT_SERDE));
    }

    /**
     * Approved orders should be enriched with User details which also includes the address of the user
     * @return
     */
    @Bean
    public BiFunction<KStream<String, Order>, KTable<String, Person>, KTable<String, Order>> enrichedOrders(){
        return (order, person) -> order
                .peek((k,v) -> LOGGER.info("processing approved order with key [{}], and value [{}]", k, v))
                .toTable();
    }

    /**
     * Enriched Orders should then be assigned to a delivery person
     * @return
     */
    @Bean
    public BiFunction<KStream<String, Order>, KTable<String, Person>, KTable<String, Order>> deliveryAssigned(){
        return (order, person) -> order
                .peek((k,v) -> LOGGER.info("processing enriched order with key [{}], and value [{}]", k, v))
                .toTable();
    }
}
