package com.appcloid.kafka.stream.example2.config;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.state.KeyValueStore;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Properties;

public class CartProcessor {
/**
    private static final Serde<ItemAddedInCart> itemAddedInCartJsonSerde = new JsonSerde<>(ItemAddedInCart.class);
    private static final Serde<CartSubmitted> cartSubmittedJsonSerde = new JsonSerde<>(CartSubmitted.class);
    private static final Serde<ItemAddedInInventory> itemAddedInInventoryJsonSerde = new JsonSerde<>(ItemAddedInInventory.class);
    private static final Serde<CartState> cartStateJsonSerde = new JsonSerde<>(CartState.class);

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "cart-processor");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        StreamsBuilder builder = new StreamsBuilder();

        // Streams for events
        KStream<String, ItemAddedInCart> itemAddedStream = builder.stream("item-added-topic",
                Consumed.with(Serdes.String(), itemAddedInCartJsonSerde));
        KStream<String, CartSubmitted> cartSubmittedStream = builder.stream("cart-submitted-topic",
                Consumed.with(Serdes.String(), cartSubmittedJsonSerde));
        KStream<String, ItemAddedInInventory> itemAddedInInventoryStream = builder.stream("item-added-inventory-topic",
                Consumed.with(Serdes.String(), itemAddedInInventoryJsonSerde));

        // State stores
        Materialized<String, CartState, KeyValueStore<Bytes, byte[]>> cartStateStore =
                Materialized.<String, CartState, KeyValueStore<Bytes, byte[]>>as("cart-state-store")
                        .withKeySerde(Serdes.String())
                        .withValueSerde(cartStateJsonSerde);

        // Process item added in cart events
        itemAddedStream
                .join(itemAddedInInventoryStream,
                        (itemAddedInCart, itemAddedInInventory) -> itemAddedInCart
                )
                .filter((customerId, itemAddedInCart) -> itemAddedInCart.getQuantityOfItem() > 0)
                .leftJoin(cartSubmittedStream,
                        (itemAddedInCart, cartSubmitted) -> new CartEventJoinResult(itemAddedInCart, cartSubmitted))
                .process(() -> new CartEventProcessor(cartStateStore),
                        Named.as("item-added-processor"),
                        cartStateStore.name())
                .to("cart-item-accepted-topic",
                        Produced.with(Serdes.String(), Serdes.serdeFrom(CartItemAccepted.serializer())));

        // Process cart submitted events
        cartSubmittedStream
                .join(itemAddedInCartTable(),
                        (cartSubmitted, itemAddedInCart) -> new CartEventJoinResult(itemAddedInCart, cartSubmitted))
                .filter((customerId, cartEventJoinResult) -> cartEventJoinResult.getItemAddedInCart() != null)
                .process(() -> new CartSubmittedProcessor(),
                        Named.as("cart-submitted-processor"));

        // Start the Kafka Streams application
        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();
    }

    private static KTable<String, ItemAddedInCart> itemAddedInCartTable() {
        return builder.table("item-added-topic",
                Materialized.<String, ItemAddedInCart, KeyValueStore<Bytes, byte[]>>as("item-added-in-cart-store")
                        .withKeySerde(Serdes.String())
                        .withValueSerde(Serdes.serdeFrom(ItemAddedInCart.serializer())));
    }
}

class CartEventProcessor implements Processor<String, CartEventJoinResult> {
    private ProcessorContext context;
    private KeyValueStore<String, CartState> cartStateStore;

    public CartEventProcessor(Materialized<String, CartState, KeyValueStore<Bytes, byte[]>> cartStateStore) {
        this.cartStateStore = cartStateStore.store();
    }

    @Override
    public void init(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public void process(String customerId, CartEventJoinResult cartEventJoinResult) {
        CartState cartState = cartStateStore.get(customerId);

        if (cartState == null) {
            cartState = new CartState(customerId);
        }

        ItemAddedInCart itemAddedInCart = cartEventJoinResult.getItemAddedInCart();
        if (itemAddedInCart != null) {
            if (cartEventJoinResult.getCartSubmitted() != null) {
                // Cart submitted event received
                OrderCreated orderCreated = new OrderCreated(itemAddedInCart.getCartId(),
                        itemAddedInCart.getItemId(), itemAddedInCart.getQuantityOfItem());
                // Process order creation logic

                // Publish OrderCreated event
                context.forward(customerId, orderCreated);
            } else {
                // Item added in cart event received
                if (itemAddedInCart.getCreationTime().isBefore(LocalDate.now().minusDays(3))) {
                    // Cart not submitted in 3 days, delete the cart
                    cartStateStore.delete(customerId);
                    // Perform additional cleanup or notification logic
                } else {
                    // Check if quantity is available in inventory
                    boolean isQuantityAvailable = checkInventory(itemAddedInCart.getItemId(),
                            itemAddedInCart.getQuantityOfItem());
                    if (isQuantityAvailable) {
                        // Quantity available, cart item accepted
                        CartItemAccepted cartItemAccepted = new CartItemAccepted(itemAddedInCart.getCartId(),
                                itemAddedInCart.getItemId(), itemAddedInCart.getQuantityOfItem());
                        // Process accepted cart item logic

                        // Publish CartItemAccepted event
                        context.forward(customerId, cartItemAccepted);
                    } else {
                        // Quantity not available, item not available event
                        ItemNotAvailable itemNotAvailable = new ItemNotAvailable(itemAddedInCart.getCartId(),
                                itemAddedInCart.getItemId(), itemAddedInCart.getQuantityOfItem());
                        // Process item not available logic

                        // Publish ItemNotAvailable event
                        context.forward(customerId, itemNotAvailable);
                        return; // Skip further processing
                    }
                }
            }
        }

        // Update the cart state in the store
        cartStateStore.put(customerId, cartState);
    }

    @Override
    public void close() {
        // Close any resources if needed
    }

    private boolean checkInventory(String itemId, int quantity) {
        // Logic to check if quantity is available in the inventory
        // Return true if quantity is available, false otherwise
    }
 **/
}

