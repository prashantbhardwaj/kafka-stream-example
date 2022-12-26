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
public class ModelOrder {
    private int id;
    private List<ModelProduct> products;
    private ModelPerson user;
    @Builder.Default
    public OrderState state = OrderState.CREATED;

    public static enum OrderState {
        CREATED, DELIVERY_ASSIGNED, REJECTED
    }
}
