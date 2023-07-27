package com.appcloid.kafka.stream.example.config;

import com.appcloid.kafka.stream.example.Constants;
import com.appcloid.kafka.stream.example.model.entities.CustomerCart;
import com.appcloid.kafka.stream.example.model.events.CartSubmittedForOrder;
import com.appcloid.kafka.stream.example.model.events.ItemAddedInCart;
import com.appcloid.kafka.stream.example.model.entities.Order;
import com.appcloid.kafka.stream.example.model.entities.Person;
import com.appcloid.kafka.stream.example.model.entities.Product;
import com.appcloid.kafka.stream.example.model.events.ProductUpdated;
import com.appcloid.kafka.stream.example.model.events.order.OrderCreatedEvent;
import com.appcloid.kafka.stream.example.model.events.order.OrderEvent;
import com.appcloid.kafka.stream.example.processors.CartItemToProductJoiner;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerde;

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
    public Function<KStream<String, ProductUpdated>, KTable<String, Product>> aggregateProducts() {
        return input -> input
                .peek((k,v) -> LOGGER.info("Received product with key [{}] and value [{}]",k, v))
                .groupByKey()
                .aggregate(Product::new,
                        (key, value, aggregate) -> aggregate.process(value),
                        Materialized.<String, Product, KeyValueStore<Bytes, byte[]>>as(PRODUCT_AGGREGATE_STATE_STORE).withValueSerde(Constants.PRODUCT_SERDE)//.withKeySerde(keySerde)
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
    public BiFunction<KStream<String, ItemAddedInCart>, KTable<String, Product>, KStream<String, ItemAddedInCart>[]> processCartItem(){
        return (cartItem, aggregatedProduct) -> cartItem
                .peek((cartItemKey, cartItemValue) -> LOGGER.info("Item in cart received with key [{}] and value [{}]", cartItemKey, cartItemValue))
                .selectKey((cartItemKey, cartItemValue) -> cartItemValue.getProductId())
                .leftJoin(
                        aggregatedProduct,
                        (item, product) -> CartItemToProductJoiner.joinForCartItem(item.getProductId(), item, product),
                        Joined.with(Constants.KEY_SERDE, Constants.ITEM_SERDE, Constants.PRODUCT_SERDE).withName(Constants.ALL_CART_ITEM_PRODUCT_JOIN_STORE)
                )
                .peek((k,v) -> LOGGER.info("for item key [{}], created order [{}]", k, v))
                .split()
                .branch((k, o) -> o.getState().isRejected(), Branched.<String, ItemAddedInCart>withConsumer(str -> str.to(Constants.OUT_REJECTED_CART_ITEMS_TOPIC, Produced.with(Constants.KEY_SERDE, Constants.ITEM_SERDE))).withName(Constants.OUT_REJECTED_CART_ITEMS_TOPIC))
                .defaultBranch(Branched.<String, ItemAddedInCart>withConsumer(str -> str.to(Constants.OUT_APPROVED_CART_ITEMS_TOPIC, Produced.with(Constants.KEY_SERDE, Constants.ITEM_SERDE))).withName(Constants.OUT_APPROVED_CART_ITEMS_TOPIC))
                .values()
                .toArray(new KStream[0]); // if you are returning a BiFunction of <KStream<String, ItemAddedInCart>, KTable<String, Product>, KStream<String, Order>>
    }

    /**
     * Now the product quantity as ordered by the user should be deducted from the product state
     * @return
     */
    @Bean
    public Function<KStream<String, ItemAddedInCart>, KTable<String, CustomerCart>> aggregateCartItemsByUserId(){
        return approvedCartItems -> approvedCartItems
                .filter((k, cartItem) -> !cartItem.getState().isRejected() && cartItem.getCustomerId() != null)
                .groupBy((k, o) -> o.getCustomerId())
                .aggregate(
                        () -> CustomerCart.builder().build(),
                        (key, itemAddedInCart, customerCart) -> customerCart.addCartItem(itemAddedInCart),
                        Materialized.<String, CustomerCart, KeyValueStore<Bytes, byte[]>>as(Constants.CUSTOMER_CART_STATE_STORE).withValueSerde(Constants.CUSTOMER_CART_SERDE)
                );
    }

    /**
     * Now the product quantity as ordered by the user should be deducted from the product state
     * @return
     */
    @Bean
    public BiFunction<KStream<String, ItemAddedInCart>, KTable<String, Product>, KStream<String, ProductUpdated>> deductItemQuantityFromProductState(){
        return (approvedItem, aggregatedProduct) -> approvedItem
                .peek((k,v) -> LOGGER.info("processing approved cart item with key [{}], and value [{}]", k, v))
                .selectKey((cartItemKey, cartItemValue) -> cartItemValue.getProductId())
                .leftJoin(
                        aggregatedProduct,
                        (item, product) -> CartItemToProductJoiner.joinForProductUpdate(item.getProductId(), item, product),
                        Joined.with(Constants.KEY_SERDE, Constants.ITEM_SERDE, Constants.PRODUCT_SERDE, Constants.PRODUCT_UPDATE_STATE_STORE)
                )
                .peek((k,v) -> LOGGER.info("after processing cart quantity updated on product with key [{}], and value [{}]", k, v));
    }

    /**
     * Submitted cart should be converted into OrderCreatedEvent
     * @return
     */
    @Bean
    public BiFunction<KStream<String, CartSubmittedForOrder>, KTable<String, CustomerCart>, KStream<String, OrderEvent>> enrichedOrders(){
        return (cartSubmittedForOrderKStream, customerCartKTable) -> cartSubmittedForOrderKStream
                .peek((k,v) -> LOGGER.info("processing approved order with key [{}], and value [{}]", k, v))
                .leftJoin(
                        customerCartKTable,
                        (cartSubmitted, custCart) -> OrderCreatedEvent.builder().build()
                );
    }

    /**
     * Created Orders should then be assigned to a delivery person
     * @return
     */
    @Bean
    public BiFunction<KStream<String, OrderEvent>, KTable<String, Order>, KTable<String, Order>> orderUpdatedBasedOnTheEvent(){
        return (orderEventKStream, orderKTable) -> orderEventKStream
                .peek((k,v) -> LOGGER.info("processing order event with key [{}], and value [{}]", k, v))
                .groupByKey()
                .aggregate(
                        () -> Order.builder().build(),
                        (orderKey, orderEvent, existingOrder) -> existingOrder
                );
    }

    /**
     * Created Orders should then be assigned to a delivery person
     * @return
     */
    @Bean
    public BiFunction<KStream<String, Order>, KTable<String, Person>, KTable<String, Order>> deliveryAssigned(){
        return (order, person) -> order
                .peek((k,v) -> LOGGER.info("processing enriched order with key [{}], and value [{}]", k, v))
                .toTable();
    }
}
