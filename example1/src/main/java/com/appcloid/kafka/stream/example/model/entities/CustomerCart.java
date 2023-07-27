package com.appcloid.kafka.stream.example.model.entities;

import com.appcloid.kafka.stream.example.model.events.ItemAddedInCart;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Builder
@Data
public class CustomerCart {
    private String cartId;
    private String customerId;
    private List<ItemAddedInCart> cartItems;
    private int parentOrderId;
    private CartStatus cartStatus;

    public CustomerCart addCartItem(ItemAddedInCart itemAddedInCart) {
        if(this.cartId == null){
            this.cartId = UUID.randomUUID().toString();
        }

        if(this.customerId == null){
            this.customerId = itemAddedInCart.getCustomerId();
        }

        if (this.cartStatus == null){
            this.cartStatus = CartStatus.CART_OPEN;
        }

        if(this.cartItems == null){
            this.cartItems = new ArrayList<>();
        }
        this.cartItems.add(itemAddedInCart);
        return this;
    }

    public enum CartStatus{
        CART_OPEN,
        CART_SUBMITTED_FOR_ORDER;
    }
}
