package com.appcloid.kafka.stream.example.model.entities;

import com.appcloid.kafka.stream.example.model.entities.Person;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Delivery {
    private String deliveryId;
    private String orderId;
    private Person deliveredBy;
}
