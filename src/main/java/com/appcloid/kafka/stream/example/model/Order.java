package com.appcloid.kafka.stream.example.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    private int id;
    private Map<Integer, Product> products;
    private Person user;
    @Builder.Default
    public OrderState state = OrderState.CREATED;

    public Order merge(Order value) {
        if(this.id == 0){
            this.id = UUID.randomUUID().hashCode();
        }

        if(this.products == null){
            this.products = new HashMap<>();
        }

        if(this.user == null){
            this.user = new Person();
        }

        if (value.getProducts() != null && value.getProducts().size() > 0){
            for (Map.Entry<Integer, Product> productEntry: value.getProducts().entrySet()) {
                if(this.products.containsKey(productEntry.getKey())){
                    Product existingProduct = this.products.get(productEntry.getKey());
                    existingProduct.setQuantity(existingProduct.getQuantity() + productEntry.getValue().getQuantity());
                    this.products.put(productEntry.getKey(), existingProduct);
                } else {
                    this.products.put(productEntry.getKey(), productEntry.getValue());
                }
            }
        }

        return null;
    }

    public static enum OrderState {
        CREATED(false, true),
        APPROVED(false, true),
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
