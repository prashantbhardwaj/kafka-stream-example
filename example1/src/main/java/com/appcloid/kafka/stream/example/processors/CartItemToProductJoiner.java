package com.appcloid.kafka.stream.example.processors;

import com.appcloid.kafka.stream.example.model.entities.Product;
import com.appcloid.kafka.stream.example.model.events.ItemAddedInCart;
import com.appcloid.kafka.stream.example.model.events.ProductUpdated;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CartItemToProductJoiner {

    public static ItemAddedInCart joinForCartItem(String productKey, ItemAddedInCart itemAddedInCart, Product product){
        log.info("joinForCartItem for productKey [{}], handling cartItem [{}]", productKey, itemAddedInCart);
        if(product != null && product.checkIfOrderQuantityAvailable(itemAddedInCart)){
            return itemAddedInCart.toBuilder().state(ItemAddedInCart.ItemAddedInCartState.APPROVED).build();
        } else {
            return itemAddedInCart.toBuilder().state(ItemAddedInCart.ItemAddedInCartState.REJECTED).build();
        }
    }

    public static ProductUpdated joinForProductUpdate(String productKey, ItemAddedInCart itemAddedInCart, Product product){
        log.info("joinForProductUpdate for productKey [{}], handling cartItem [{}]", productKey, itemAddedInCart);

        if (product == null || itemAddedInCart == null){
            return ProductUpdated.builder().productId(productKey).productUpdateType(ProductUpdated.ProductUpdateType.NO_QUANTITY_CHANGE).build();
        }

        if(itemAddedInCart.getState() == ItemAddedInCart.ItemAddedInCartState.APPROVED) {
            if(product.checkIfOrderQuantityAvailable(itemAddedInCart)){
                return ProductUpdated.builder().productId(productKey).productUpdateType(ProductUpdated.ProductUpdateType.QUANTITY_BOUGHT).quantityChange(itemAddedInCart.getQuantity()).build();
            }
        }
        return ProductUpdated.builder().productId(productKey).productUpdateType(ProductUpdated.ProductUpdateType.NO_QUANTITY_CHANGE).build();
    }
}
