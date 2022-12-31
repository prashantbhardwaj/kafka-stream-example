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
    private CartItemState state;
    private int parentOrderId;

    public static enum CartItemState {
        CREATED(false),
        ACCEPTED(false),
        GROUPED_IN_ORDER(false),
        REJECTED_QTY_UNAVAILABLE(true),
        REJECTED_PRODUCT_NOT_FOUND(true),
        DELIVERED(false),
        REMOVED(true);

        private boolean isRejected;

        CartItemState(boolean rejected){
            this.isRejected = rejected;
        }

        public boolean isRejected(){
            return this.isRejected;
        }

    }
}
