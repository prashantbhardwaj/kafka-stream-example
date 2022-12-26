package com.appcloid.kafka.stream.example.config;

import com.appcloid.kafka.stream.example.model.*;
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
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class ConfigStreamTopology implements InitializingBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigStreamTopology.class);


    @Override
    public void afterPropertiesSet() throws Exception {
        KafkaStreams kafkaStreams = new KafkaStreams(buildTopology(), getConfiguration());
        kafkaStreams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(kafkaStreams::close));
    }

    public static final String IN_PERSONS = "persons";
    public static final String IN_PRODUCTS = "products";
    public static final String IN_ORDERS = "orders";

    public static final String PRODUCT_INVENTORY_CACHE = "product_inventory";

    public static final String OUT_REJECTED_ORDERS = "rejected-orders";
    public static final String OUT_DELIVERIES = "deliveries";
    public static final String OUT_PRODUCT = "product_inventory";

    public static Topology buildTopology() {
        Serde<ModelDelivery> deliverySerdes = new JsonSerde<>(ModelDelivery.class);
        Serde<ModelAddress> addressSerde = new JsonSerde<>(ModelAddress.class);
        Serde<ModelPerson> personSerdes = new JsonSerde<>(ModelPerson.class);
        Serde<ModelOrder> orderSerde = new JsonSerde<>(ModelOrder.class);
        Serde<ModelProduct> productSerde = new JsonSerde<>(ModelProduct.class);

        Serde<String> keySerding = Serdes.String();

        StreamsBuilder streamsBuilder = new StreamsBuilder();

        KStream<String, ModelProduct> productStream = streamsBuilder
                .stream(IN_PRODUCTS, Consumed.with(keySerding, productSerde))
                .peek((k,v) -> LOGGER.info("Received product : {}", v))
                .groupByKey()
                //.groupBy((k,v) -> v.getId())
                .aggregate(ModelProduct::new,
                        (key, value, aggregate) -> aggregate.process(value),
                        Materialized.with(keySerding, productSerde))
                .toStream();

        productStream
                .to(OUT_PRODUCT, Produced.with(keySerding, productSerde));

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
