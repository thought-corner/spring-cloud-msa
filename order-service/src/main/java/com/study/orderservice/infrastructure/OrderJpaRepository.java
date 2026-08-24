package com.study.orderservice.infrastructure;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface OrderJpaRepository extends CrudRepository<OrderJpaEntity, Long> {

	Optional<OrderJpaEntity> findByOrderId(String orderId);

	List<OrderJpaEntity> findByUserId(String userId);
}
