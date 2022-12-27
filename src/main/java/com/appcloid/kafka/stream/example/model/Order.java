package com.appcloid.kafka.stream.example.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    private int id;
    private List<Product> products;
    private Person user;
    @Builder.Default
    public OrderState state = OrderState.CREATED;

    public static enum OrderState {
        CREATED, APPROVED, DELIVERY_ASSIGNED, REJECTED_QTY_UNAVAILABLE, REJECTED_PRODUCT_NOT_FOUND
    }
}
