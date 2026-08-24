package com.study.orderservice.presentation;

import com.study.orderservice.application.PlaceOrderCommand;
import jakarta.validation.constraints.NotNull;

public record PlaceOrderRequest(
        @NotNull(message = "productId must not be null") String productId,
        @NotNull(message = "qty must not be null") Integer qty,
        @NotNull(message = "unitPrice must not be null") Long unitPrice) {

    public PlaceOrderCommand toCommand(String userId) {
        return new PlaceOrderCommand(userId, productId, qty, unitPrice);
    }
}
