package com.study.orderservice.infrastructure;

import com.study.orderservice.domain.Money;
import com.study.orderservice.domain.Order;
import com.study.orderservice.domain.OrderId;
import com.study.orderservice.domain.ProductId;
import com.study.orderservice.domain.Quantity;
import com.study.orderservice.domain.UserId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "orders")
public class OrderJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, unique = true)
    private String orderId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "product_id", nullable = false, length = 120)
    private String productId;

    @Column(name = "qty", nullable = false)
    private int qty;

    @Column(name = "unit_price", nullable = false)
    private long unitPrice;

    @Column(name = "total_price", nullable = false)
    private long totalPrice;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected OrderJpaEntity() {
    }

    private OrderJpaEntity(String orderId, String userId, String productId, int qty,
                           long unitPrice, long totalPrice, Instant createdAt) {
        this.orderId = orderId;
        this.userId = userId;
        this.productId = productId;
        this.qty = qty;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
        this.createdAt = createdAt;
    }

    static OrderJpaEntity from(Order order) {
        return new OrderJpaEntity(
                order.orderId().value(),
                order.userId().value(),
                order.productId().value(),
                order.quantity().value(),
                order.unitPrice().amount(),
                order.totalPrice().amount(),
                order.createdAt());
    }

    Order toDomain() {
        return Order.reconstitute(
                OrderId.of(orderId),
                UserId.of(userId),
                ProductId.of(productId),
                Quantity.of(qty),
                Money.of(unitPrice),
                Money.of(totalPrice),
                createdAt);
    }
}
