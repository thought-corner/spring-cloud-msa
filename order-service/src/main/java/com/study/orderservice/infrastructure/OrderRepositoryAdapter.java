package com.study.orderservice.infrastructure;

import com.study.orderservice.domain.Order;
import com.study.orderservice.domain.OrderId;
import com.study.orderservice.domain.OrderRepository;
import com.study.orderservice.domain.UserId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class OrderRepositoryAdapter implements OrderRepository {

	private final OrderJpaRepository orderJpaRepository;

	public OrderRepositoryAdapter(OrderJpaRepository orderJpaRepository) {
		this.orderJpaRepository = orderJpaRepository;
	}

	@Override
	public Order save(Order order) {
		return orderJpaRepository.save(OrderJpaEntity.from(order)).toDomain();
	}

	@Override
	public Optional<Order> findByOrderId(OrderId orderId) {
		return orderJpaRepository.findByOrderId(orderId.value()).map(OrderJpaEntity::toDomain);
	}

	@Override
	public List<Order> findByUserId(UserId userId) {
		return orderJpaRepository.findByUserId(userId.value()).stream()
				.map(OrderJpaEntity::toDomain)
				.toList();
	}
}
