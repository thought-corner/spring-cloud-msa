package com.study.orderservice.presentation;

import com.study.orderservice.application.OrderResult;
import com.study.orderservice.application.OrderService;
import com.study.orderservice.application.PlaceOrderCommand;
import com.study.common.security.TokenVerifier;
import com.study.orderservice.security.WebSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import(WebSecurityConfig.class)
@ActiveProfiles("test")
class OrderControllerTest {

    private static final String BODY = "{\"productId\":\"PRODUCT-001\",\"qty\":2,\"unitPrice\":1000}";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private TokenVerifier tokenVerifier;

    @Test
    void 인증되지_않은_요청은_거부된다() throws Exception {
        mockMvc.perform(post("/orders/{userId}", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isForbidden());

        verify(orderService, never()).place(any(PlaceOrderCommand.class));
    }

    @Test
    void 본인_식별자로_주문을_생성하면_201을_반환한다() throws Exception {
        when(orderService.place(any())).thenReturn(sampleResult("user-1"));

        mockMvc.perform(post("/orders/{userId}", "user-1").with(user("user-1"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isCreated());
    }

    @Test
    void 타인의_식별자로_주문을_생성하면_403으로_거부하고_서비스를_호출하지_않는다() throws Exception {
        mockMvc.perform(post("/orders/{userId}", "user-1").with(user("attacker"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isForbidden());

        verify(orderService, never()).place(any(PlaceOrderCommand.class));
    }

    @Test
    void 본인_식별자로_주문_목록을_조회하면_200을_반환한다() throws Exception {
        when(orderService.findByUserId("user-1")).thenReturn(List.of(sampleResult("user-1")));

        mockMvc.perform(get("/orders").param("userId", "user-1").with(user("user-1")))
                .andExpect(status().isOk());
    }

    @Test
    void 타인의_주문_목록을_조회하면_403으로_거부하고_서비스를_호출하지_않는다() throws Exception {
        mockMvc.perform(get("/orders").param("userId", "user-1").with(user("attacker")))
                .andExpect(status().isForbidden());

        verify(orderService, never()).findByUserId(any());
    }

    @Test
    void 본인_소유_주문을_주문번호로_조회하면_200을_반환한다() throws Exception {
        when(orderService.findByOrderId("ORDER-1")).thenReturn(Optional.of(sampleResult("user-1")));

        mockMvc.perform(get("/orders/{orderId}", "ORDER-1").with(user("user-1")))
                .andExpect(status().isOk());
    }

    @Test
    void 타인_소유_주문을_주문번호로_조회하면_404로_숨긴다() throws Exception {
        when(orderService.findByOrderId("ORDER-1")).thenReturn(Optional.of(sampleResult("user-1")));

        mockMvc.perform(get("/orders/{orderId}", "ORDER-1").with(user("attacker")))
                .andExpect(status().isNotFound());
    }

    @Test
    void 존재하지_않는_주문을_주문번호로_조회하면_404를_반환한다() throws Exception {
        when(orderService.findByOrderId("NO-SUCH")).thenReturn(Optional.empty());

        mockMvc.perform(get("/orders/{orderId}", "NO-SUCH").with(user("user-1")))
                .andExpect(status().isNotFound());
    }

    private static OrderResult sampleResult(String userId) {
        return new OrderResult("ORDER-1", userId, "PRODUCT-001", 2, 1000, 2000, Instant.parse("2026-01-01T00:00:00Z"));
    }
}
