package com.appcloid.kafka.stream.example.model.events;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CartSubmittedForOrder {
    private String customerId;
    private String cartId;
}
