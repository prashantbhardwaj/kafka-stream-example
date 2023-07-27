package com.appcloid.kafka.stream.example.model.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemAddedInCart {
    private String customerId;
    private String productId;
    private int quantity;
    private ItemAddedInCartState state;
    private ItemAddedInCartType type;

    public String getId(){
        return this.customerId + "-" + this.productId;
    }

    public enum ItemAddedInCartState {
        APPROVED(false),
        REJECTED(true);

        private boolean isRejected;

        ItemAddedInCartState(boolean rejected){
            this.isRejected = rejected;
        }

        public boolean isRejected(){
            return this.isRejected;
        }

    }

    public enum ItemAddedInCartType {
        QUANTITY_UPDATED,
        ITEM_REMOVED;
    }
}
