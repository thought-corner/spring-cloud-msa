package com.study.orderservice.presentation;

import com.study.orderservice.application.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // authN(신원 확인)은 JwtAuthenticationFilter 가 끝냈다. 여기서는 authZ(소유권)만 판정한다:
    // 인증된 본인만 자기 자원을 다룰 수 있고, 타인 식별자를 지정하면 403 으로 거부한다.
    @PreAuthorize("#userId == authentication.name")
    @PostMapping("/{userId}")
    public ResponseEntity<OrderResponse> placeOrder(@PathVariable("userId") String userId,
                                                    @Valid @RequestBody PlaceOrderRequest request) {
        OrderResponse response = OrderResponse.from(orderService.place(request.toCommand(userId)));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("#userId == authentication.name")
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrders(@RequestParam("userId") String userId) {
        List<OrderResponse> result = orderService.findByUserId(userId).stream()
                .map(OrderResponse::from)
                .toList();
        return ResponseEntity.ok(result);
    }

    // 소유자는 주문을 로드해야 알 수 있어 @PreAuthorize 로 선언할 수 없다. 로드 후 본인 소유가
    // 아니면 존재를 숨기기 위해 404 로 응답한다(타인 주문의 존재 여부를 노출하지 않는다).
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(Principal principal,
                                                  @PathVariable("orderId") String orderId) {
        return orderService.findByOrderId(orderId)
                .filter(result -> result.userId().equals(principal.getName()))
                .map(result -> ResponseEntity.ok(OrderResponse.from(result)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
