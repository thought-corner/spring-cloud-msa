package com.study.orderservice.presentation;

import com.study.orderservice.application.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@PostMapping("/{userId}")
	public ResponseEntity<OrderResponse> placeOrder(@PathVariable("userId") String userId,
													@Valid @RequestBody PlaceOrderRequest request) {
		OrderResponse response = OrderResponse.from(orderService.place(request.toCommand(userId)));

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping
	public ResponseEntity<List<OrderResponse>> getOrders(@RequestParam("userId") String userId) {
		List<OrderResponse> result = orderService.findByUserId(userId).stream()
				.map(OrderResponse::from)
				.toList();
		return ResponseEntity.ok(result);
	}

	@GetMapping("/{orderId}")
	public ResponseEntity<OrderResponse> getOrder(@PathVariable("orderId") String orderId) {
		return orderService.findByOrderId(orderId)
				.map(result -> ResponseEntity.ok(OrderResponse.from(result)))
				.orElseGet(() -> ResponseEntity.notFound().build());
	}
}
