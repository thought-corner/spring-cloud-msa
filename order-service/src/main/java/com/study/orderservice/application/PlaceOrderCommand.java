package com.study.orderservice.application;

public record PlaceOrderCommand(String userId, String productId, int quantity, long unitPrice) {
}
