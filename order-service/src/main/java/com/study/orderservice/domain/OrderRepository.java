package com.study.orderservice.domain;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {

	Order save(Order order);

	Optional<Order> findByOrderId(OrderId orderId);

	List<Order> findByUserId(UserId userId);
}
