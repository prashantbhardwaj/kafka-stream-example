package com.appcloid.kafka.stream.example.model.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Person {
    private int id;
    private String name;
    private Address address;
}
