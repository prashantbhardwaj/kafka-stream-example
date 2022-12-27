package com.appcloid.kafka.stream.example.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

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
        if (order.getProducts().contains(this)){
            int quantityOrdered = order.getProducts().stream().filter(o -> o.getId() == this.id).map(Product::getQuantity).findAny().orElse(0);
            this.quantity = this.quantity - quantityOrdered;
        }

        return this;
    }

    public Order checkIfOrderQuantityAvailable(ItemAddedInCart itemAddedInCart) {
        LOG.info(this.toString());
        Order.OrderBuilder order = Order.builder().id(UUID.randomUUID().hashCode());
        if (itemAddedInCart.getProductId() == this.id){
            if(itemAddedInCart.getQuantity() > this.quantity){
                return order.state(Order.OrderState.REJECTED_QTY_UNAVAILABLE).build();
            } /**else {
                this.quantity = this.quantity - itemAddedInCart.getQuantity();
            }**/
        } else {
            return order.state(Order.OrderState.REJECTED_PRODUCT_NOT_FOUND).build();
        }
        return order.state(Order.OrderState.APPROVED).build();
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
}
