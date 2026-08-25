package com.study.userservice.client;

import com.study.userservice.client.dto.OrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "order-service")
public interface OrderServiceClient {

    @GetMapping("/orders")
    List<OrderResponse> getOrders(@RequestHeader("X-Authenticated-User") String authenticatedUser,
                                  @RequestParam("userId") String userId);
}
