package com.appcloid.kafka.stream.example.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModelProduct {
    private int id;
    private String name;
    private int quantity;

    public ModelProduct process(ModelProduct value) {
        this.id = value.getId();
        this.name = value.getName();
        this.quantity = this.quantity + value.getQuantity();
        return this;
    }
}
