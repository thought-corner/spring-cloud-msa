package com.study.userservice.client.dto;

import java.time.Instant;

public record OrderResponse(
        String productId,
        Integer qty,
        Long unitPrice,
        Long totalPrice,
        Instant createdAt,
        String orderId) {
}
