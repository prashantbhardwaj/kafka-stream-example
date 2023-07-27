package com.appcloid.kafka.stream.example.model.events;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ProductUpdated {
    private String eventId;
    private String productId;
    private int quantityChange;
    private ProductUpdateType productUpdateType;

    public enum ProductUpdateType{
        QUANTITY_ADDED,
        QUANTITY_BOUGHT,
        NO_QUANTITY_CHANGE;
    }
}


