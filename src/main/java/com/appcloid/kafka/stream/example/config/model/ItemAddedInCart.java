package com.appcloid.kafka.stream.example.config.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ItemAddedInCart {
    private int quantityOfItem;
}
