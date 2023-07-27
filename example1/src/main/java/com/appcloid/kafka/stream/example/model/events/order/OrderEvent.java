package com.appcloid.kafka.stream.example.model.events.order;

public interface OrderEvent {
    String getOrderId();
    OrderEventType getEventType();

    enum OrderEventType {
        ORDER_CREATED_EVENT;
    }
}
