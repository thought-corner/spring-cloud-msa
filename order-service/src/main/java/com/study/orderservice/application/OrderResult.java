package com.study.orderservice.application;

import com.study.orderservice.domain.Order;

import java.time.Instant;

public record OrderResult(
        String orderId,
        String productId,
        int quantity,
        long unitPrice,
        long totalPrice,
        Instant createdAt) {

    public static OrderResult from(Order order) {
        return new OrderResult(
                order.orderId().value(),
                order.productId().value(),
                order.quantity().value(),
                order.unitPrice().amount(),
                order.totalPrice().amount(),
                order.createdAt());
    }
}
