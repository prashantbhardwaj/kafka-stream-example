package com.appcloid.kafka.stream.example.config;

import com.appcloid.kafka.stream.example.model.Product;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.function.Consumer;

import static com.appcloid.kafka.stream.example.Constants.PRODUCT_AGGREGATE_STATE_STORE;

@Configuration
public class SpringStreamBinderTopologyBuilderConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpringStreamBinderTopologyBuilderConfig.class);

    @Bean
    public Consumer<KStream<String, Product>> aggregateProducts() {
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
}
