package com.appcloid.kafka.stream.example.controller;

import com.appcloid.kafka.stream.example.model.Product;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.appcloid.kafka.stream.example.Constants.PRODUCT_AGGREGATE_STATE_STORE;

@RestController
public class ProductAggregateDetailsController {
    @Autowired
    private InteractiveQueryService interactiveQueryService;

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
