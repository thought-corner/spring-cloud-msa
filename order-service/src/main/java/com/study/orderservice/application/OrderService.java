package com.study.orderservice.application;

import com.study.orderservice.domain.Money;
import com.study.orderservice.domain.Order;
import com.study.orderservice.domain.OrderId;
import com.study.orderservice.domain.OrderRepository;
import com.study.orderservice.domain.ProductId;
import com.study.orderservice.domain.Quantity;
import com.study.orderservice.domain.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final Clock clock;

    public OrderService(OrderRepository orderRepository, Clock clock) {
        this.orderRepository = orderRepository;
        this.clock = clock;
    }

    @Transactional
    public OrderResult place(PlaceOrderCommand command) {
        Order order = Order.place(UserId.of(command.userId()), ProductId.of(command.productId()),
                Quantity.of(command.quantity()), Money.of(command.unitPrice()), clock.instant());
        return OrderResult.from(orderRepository.save(order));
    }

    public Optional<OrderResult> findByOrderId(String orderId) {
        return orderRepository.findByOrderId(OrderId.of(orderId)).map(OrderResult::from);
    }

    public List<OrderResult> findByUserId(String userId) {
        return orderRepository.findByUserId(UserId.of(userId)).stream()
                .map(OrderResult::from)
                .toList();
    }
}
