package com.appcloid.kafka.stream.example.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    private int productId;
    private int userId;
    private int quantity;
}
