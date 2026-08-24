package com.study.orderservice.domain;

import java.time.Instant;

public class Order {

    private final OrderId orderId;
    private final UserId userId;
    private final ProductId productId;
    private final Quantity quantity;
    private final Money unitPrice;
    private final Money totalPrice;
    private final Instant createdAt;

    private Order(OrderId orderId,
                  UserId userId,
                  ProductId productId,
                  Quantity quantity,
                  Money unitPrice,
                  Money totalPrice,
                  Instant createdAt) {
        this.orderId = orderId;
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
        this.createdAt = createdAt;
    }

    public static Order place(UserId userId, ProductId productId, Quantity quantity,
                              Money unitPrice, Instant createdAt) {
        if (!unitPrice.isPositive()) {
            throw new OrderException(OrderErrorCode.NON_POSITIVE_UNIT_PRICE);
        }
        return new Order(OrderId.generate(), userId, productId, quantity, unitPrice, unitPrice.multiply(quantity), createdAt);
    }

    public static Order reconstitute(OrderId orderId,
                                     UserId userId,
                                     ProductId productId,
                                     Quantity quantity,
                                     Money unitPrice,
                                     Money totalPrice,
                                     Instant createdAt) {
        return new Order(orderId, userId, productId, quantity, unitPrice, totalPrice, createdAt);
    }

    public OrderId orderId() {
        return orderId;
    }

    public UserId userId() {
        return userId;
    }

    public ProductId productId() {
        return productId;
    }

    public Quantity quantity() {
        return quantity;
    }

    public Money unitPrice() {
        return unitPrice;
    }

    public Money totalPrice() {
        return totalPrice;
    }

    public Instant createdAt() {
        return createdAt;
    }
}
