package com.appcloid.kafka.stream.example.config;

import com.appcloid.kafka.stream.example.model.entities.*;
import com.appcloid.kafka.stream.example.model.events.ItemAddedInCart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.apache.kafka.streams.KafkaStreams;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Profile("without-spring-stream")
@Service
public class SimpleTopologyBuilderConfig implements InitializingBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleTopologyBuilderConfig.class);


    @Override
    public void afterPropertiesSet() throws Exception {
        KafkaStreams kafkaStreams = new KafkaStreams(buildTopology(), getConfiguration());
        kafkaStreams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(kafkaStreams::close));
    }

    public static final String IN_PERSONS = "persons";
    public static final String PRODUCTS_QUANTITY_UPDATED = "products_quantity_updated";
    public static final String ITEM_ADDED_IN_CART = "item_in_cart";
    public static final String IN_ORDERS = "orders_in";

    public static final String PRODUCT_INVENTORY_CACHE = "product_inventory";

    public static final String OUT_REJECTED_CART_ITEMS = "rejected-cart-items";
    public static final String OUT_REJECTED_ORDERS = "rejected-orders";
    public static final String OUT_DELIVERIES = "deliveries_out";
    public static final String OUT_PRODUCT = "product_inventory_out";
    public static final String OUT_ORDERS = "orders_out";

    public static Topology buildTopology() {
        Serde<Delivery> deliverySerde = new JsonSerde<>(Delivery.class);
        Serde<Address> addressSerde = new JsonSerde<>(Address.class);
        Serde<Person> personSerde = new JsonSerde<>(Person.class);
        Serde<Order> orderSerde = new JsonSerde<>(Order.class);
        Serde<Product> productSerde = new JsonSerde<>(Product.class);
        Serde<ItemAddedInCart> cartItemSerde = new JsonSerde<>(ItemAddedInCart.class);

        Serde<String> keySerde = Serdes.String();

        StreamsBuilder streamsBuilder = new StreamsBuilder();

        KTable<String, Product> productTable = streamsBuilder
                .stream(PRODUCTS_QUANTITY_UPDATED, Consumed.with(keySerde, productSerde))
                .peek((k,v) -> LOGGER.info("Received product : {}", v))
                .groupByKey()
                .aggregate(Product::new,
                        (key, value, aggregate) -> aggregate.process(value),
                        Materialized.with(keySerde, productSerde));

        ValueJoiner<ItemAddedInCart, Product, Order> orderQualifier =
                (item, product) -> product != null ? new Order() : Order.builder().state(Order.OrderState.REJECTED_PRODUCT_NOT_FOUND).build();

        streamsBuilder.stream(ITEM_ADDED_IN_CART, Consumed.with(keySerde, cartItemSerde))
                .leftJoin(
                        productTable,
                        orderQualifier,
                        Joined.with(keySerde, cartItemSerde, productSerde)
                )
                .peek((k,v) -> LOGGER.info("Received order {} : {}", k, v))
                .filter((k, o) -> o.getState().isRejected())
                .to(OUT_REJECTED_ORDERS, Produced.with(keySerde, orderSerde));

        productTable
                .toStream()
                .to(OUT_PRODUCT, Produced.with(keySerde, productSerde));

        return streamsBuilder.build();
    }

    public static Properties getConfiguration() {
        Properties properties = new Properties();
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "ecommerce");
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "0");
        return properties;
    }
}
