package com.study.orderservice.presentation;

import com.study.orderservice.application.OrderResult;

import java.time.Instant;

public record OrderResponse(
        String productId,
        Integer qty,
        Long unitPrice,
        Long totalPrice,
        Instant createdAt,
        String orderId) {

    public static OrderResponse from(OrderResult result) {
        return new OrderResponse(
                result.productId(),
                result.quantity(),
                result.unitPrice(),
                result.totalPrice(),
                result.createdAt(),
                result.orderId());
    }
}
