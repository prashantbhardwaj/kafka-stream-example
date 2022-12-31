package com.appcloid.kafka.stream.example.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private static final Logger LOG = LoggerFactory.getLogger(Product.class);
    private int id;
    private String name;
    private int quantity;

    public Product process(Product value) {
        this.id = value.getId();
        this.name = value.getName();
        this.quantity = this.quantity + value.getQuantity();
        return this;
    }

    public Product deductOrderedQuantity(Order order) {
        if (!CollectionUtils.isEmpty(order.getProducts()) && order.getProducts().containsValue(this)){
            int quantityOrdered = order.getProducts().values().stream()
                    .filter(o -> o.getId() == this.id)
                    .map(Product::getQuantity).findAny().orElse(0);
            this.quantity = this.quantity - quantityOrdered;
        }

        return this;
    }

    public Order checkIfOrderQuantityAvailableAndCreateOrder(CartItem cartItem) {
        LOG.info(this.toString());
        Person user = Person.builder().id(cartItem.getUserId()).build();
        Product copyProduct = this.copy();
        Map<Integer, Product> products = new HashMap<>();
        products.put(copyProduct.id, copyProduct);

        Order.OrderBuilder order = Order.builder().id(UUID.randomUUID().hashCode()).user(user);

        if (cartItem.getProductId() == this.id){
            if(cartItem.getQuantity() > this.quantity){
                return order.state(Order.OrderState.REJECTED_QTY_UNAVAILABLE).build();
            } /**else {
                this.quantity = this.quantity - itemAddedInCart.getQuantity();
            }**/
        } else {
            return order.state(Order.OrderState.REJECTED_PRODUCT_NOT_FOUND).build();
        }

        return order.products(products).state(Order.OrderState.APPROVED).build();
    }

    public CartItem checkIfOrderQuantityAvailable(CartItem cartItem) {
        LOG.info(this.toString());

        cartItem.setState(CartItem.CartItemState.ACCEPTED);

        if (cartItem.getProductId() == this.id){
            if(cartItem.getQuantity() > this.quantity){
                cartItem.setState(CartItem.CartItemState.REJECTED_QTY_UNAVAILABLE);
            } /**else {
             this.quantity = this.quantity - itemAddedInCart.getQuantity();
             }**/
        } else {
            cartItem.setState(CartItem.CartItemState.REJECTED_PRODUCT_NOT_FOUND);
        }

        return cartItem;
    }

    public boolean equals(Object product){
        if(product == null || !(product instanceof Product) || ((Product)product).id != this.id){
            return false;
        }
        return true;
    }

    public int hashCode(){
        return new Integer(this.id).hashCode();
    }

    private Product copy(){
        return Product.builder().id(this.id).name(this.name).build();
    }
}
