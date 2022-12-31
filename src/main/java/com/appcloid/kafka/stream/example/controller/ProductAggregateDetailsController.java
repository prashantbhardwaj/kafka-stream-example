package com.appcloid.kafka.stream.example.controller;

import com.appcloid.kafka.stream.example.Constants;
import com.appcloid.kafka.stream.example.model.Order;
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
    public List<Product> products() {

        final ReadOnlyKeyValueStore<String, Product> productStore =
                interactiveQueryService.getQueryableStore(PRODUCT_AGGREGATE_STATE_STORE, QueryableStoreTypes.<String, Product>keyValueStore());
        Iterable<KeyValue<String, Product>> iterable = () -> productStore.all();
        return StreamSupport.stream(iterable.spliterator(), false).map(kv -> kv.value).collect(Collectors.toList());
    }

    @RequestMapping("/products/{product_id}")
    public Product productByProductId(@PathVariable("product_id") String productId) {

        final ReadOnlyKeyValueStore<String, Product> productStore =
                interactiveQueryService.getQueryableStore(PRODUCT_AGGREGATE_STATE_STORE, QueryableStoreTypes.<String, Product>keyValueStore());
        return productStore.get(productId);
    }

    @RequestMapping("/orders")
    public List<Order> orders() {

        final ReadOnlyKeyValueStore<String, Order> orderStore =
                interactiveQueryService.getQueryableStore(Constants.ORDER_STATE_STORE, QueryableStoreTypes.<String, Order>keyValueStore());
        Iterable<KeyValue<String, Order>> iterable = () -> orderStore.all();
        return StreamSupport.stream(iterable.spliterator(), false).map(kv -> kv.value).collect(Collectors.toList());
    }
}
