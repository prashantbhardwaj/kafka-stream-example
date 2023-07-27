package com.appcloid.kafka.stream.example.model.entities;

import com.appcloid.kafka.stream.example.model.events.ItemAddedInCart;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    private String orderId;
    private String customerCartId;
    @Builder.Default
    public OrderState state = OrderState.CREATED;

    public enum OrderState {
        CREATED(false, true),
        APPROVED(false, true),
        PAID(false, false),
        DELIVERY_ASSIGNED(false, false),
        REJECTED_QTY_UNAVAILABLE(true, false),
        REJECTED_PRODUCT_NOT_FOUND(true, false),
        DELIVERED(false, false),
        CANCELLED(false, false);

        private boolean isRejected;
        private boolean isOpen;

        OrderState(boolean rejected, boolean open){
            this.isRejected = rejected;
            this.isOpen = open;
        }

        public boolean isRejected(){
            return this.isRejected;
        }

        public boolean isOpen(){
            return this.isOpen;
        }
    }
}
