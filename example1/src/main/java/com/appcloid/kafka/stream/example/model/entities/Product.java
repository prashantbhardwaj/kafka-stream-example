package com.appcloid.kafka.stream.example.model.entities;

import com.appcloid.kafka.stream.example.model.events.ItemAddedInCart;
import com.appcloid.kafka.stream.example.model.events.ProductUpdated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private static final Logger LOG = LoggerFactory.getLogger(Product.class);
    private String id;
    private String name;
    private int quantity;

    public Product process(ProductUpdated value) {
        if(value.getProductUpdateType() == ProductUpdated.ProductUpdateType.QUANTITY_ADDED && value.getQuantityChange() >= 0) {
            this.quantity = this.quantity + value.getQuantityChange();
        }

        if (value.getProductUpdateType() == ProductUpdated.ProductUpdateType.QUANTITY_BOUGHT && this.quantity >= value.getQuantityChange()){
            this.quantity = this.quantity - value.getQuantityChange();
        }

        return this;
    }

    public boolean checkIfOrderQuantityAvailable(ItemAddedInCart itemAddedInCart) {
        if (itemAddedInCart.getProductId() == this.id){
            if(itemAddedInCart.getQuantity() >= this.quantity){
                return true;
            }
        }
        return false;
    }

    public boolean equals(Object product){
        if(product == null || !(product instanceof Product) || ((Product)product).id != this.id){
            return false;
        }
        return true;
    }

    public int hashCode(){
        return this.id.hashCode();
    }

    private Product copy(){
        return Product.builder().id(this.id).name(this.name).build();
    }
}
