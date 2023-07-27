package com.appcloid.kafka.stream.example.model.events.order;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class OrderCreatedEvent implements OrderEvent{
    private String orderId;

    @Override
    public String getOrderId() {
        return this.orderId;
    }

    @Override
    public OrderEventType getEventType() {
        return OrderEventType.ORDER_CREATED_EVENT;
    }
}
