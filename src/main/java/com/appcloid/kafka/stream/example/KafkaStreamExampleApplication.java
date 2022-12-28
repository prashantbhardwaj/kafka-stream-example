package com.appcloid.kafka.stream.example;

import com.appcloid.kafka.stream.example.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@SpringBootApplication
public class KafkaStreamExampleApplication {
	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaStreamExampleApplication.class);
	private static final String PRODUCT_AGGREGATE_STATE_STORE = "product_aggregate_state";

	@Autowired
	private InteractiveQueryService interactiveQueryService;

	public static void main(String[] args) {
		SpringApplication.run(KafkaStreamExampleApplication.class, args);
	}

	public static class KafkaStreamsAggregateSampleApplication {
		Serde<Delivery> deliverySerdes = new JsonSerde<>(Delivery.class);
		Serde<Address> addressSerde = new JsonSerde<>(Address.class);
		Serde<Person> personSerdes = new JsonSerde<>(Person.class);
		Serde<Order> orderSerde = new JsonSerde<>(Order.class);
		//Serde<Product> productSerde = new JsonSerde<>(Product.class);
		Serde<ItemAddedInCart> itemSerde = new JsonSerde<>(ItemAddedInCart.class);
		Serde<String> keySerde = Serdes.String();

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

	@RestController
	public class ProductAggregateDetailsController {

		@RequestMapping("/products")
		public List<Product> events() {

			final ReadOnlyKeyValueStore<String, Product> topFiveStore =
					interactiveQueryService.getQueryableStore(PRODUCT_AGGREGATE_STATE_STORE, QueryableStoreTypes.<String, Product>keyValueStore());
			Iterable<KeyValue<String, Product>> iterable = () -> topFiveStore.all();
			return StreamSupport.stream(iterable.spliterator(), false).map(kv -> kv.value).collect(Collectors.toList());
		}

		@RequestMapping("/products/{product_id}")
		public Product eventByProductId(@PathVariable("product_id") String productId) {

			final ReadOnlyKeyValueStore<String, Product> topFiveStore =
					interactiveQueryService.getQueryableStore(PRODUCT_AGGREGATE_STATE_STORE, QueryableStoreTypes.<String, Product>keyValueStore());
			return topFiveStore.get(productId);
		}
	}

}
