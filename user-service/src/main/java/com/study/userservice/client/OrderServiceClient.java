package com.study.userservice.client;

import com.study.userservice.client.dto.OrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "order-service")
public interface OrderServiceClient {

    // 게이트웨이를 거치지 않는 내부 호출이므로 호출자의 JWT를 그대로 릴레이한다.
    // order-service 가 이 토큰을 직접 검증(authN)하고 소유권(authZ)을 판정한다.
    @GetMapping("/orders")
    List<OrderResponse> getOrders(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                  @RequestParam("userId") String userId);
}
