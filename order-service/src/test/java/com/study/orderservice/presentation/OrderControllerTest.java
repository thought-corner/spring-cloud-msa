package com.study.orderservice.presentation;

import com.study.orderservice.application.OrderResult;
import com.study.orderservice.application.OrderService;
import com.study.orderservice.application.PlaceOrderCommand;
import com.study.orderservice.security.OwnDataAccessAspect;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import(OwnDataAccessAspect.class)
@EnableAspectJAutoProxy
class OrderControllerTest {

    private static final String AUTHENTICATED_USER_HEADER = "X-Authenticated-User";
    private static final String BODY = "{\"productId\":\"PRODUCT-001\",\"qty\":2,\"unitPrice\":1000}";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    void 본인_식별자로_주문을_생성하면_201을_반환한다() throws Exception {
        when(orderService.place(any())).thenReturn(sampleResult());

        mockMvc.perform(post("/orders/{userId}", "user-1")
                        .header(AUTHENTICATED_USER_HEADER, "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isCreated());
    }

    @Test
    void 타인의_식별자로_주문을_생성하면_403으로_거부하고_서비스를_호출하지_않는다() throws Exception {
        mockMvc.perform(post("/orders/{userId}", "user-1")
                        .header(AUTHENTICATED_USER_HEADER, "attacker")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isForbidden());

        verify(orderService, never()).place(any(PlaceOrderCommand.class));
    }

    @Test
    void 신원_헤더가_없으면_주문_생성이_403으로_거부된다() throws Exception {
        mockMvc.perform(post("/orders/{userId}", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isForbidden());

        verify(orderService, never()).place(any(PlaceOrderCommand.class));
    }

    @Test
    void 본인_식별자로_주문_목록을_조회하면_200을_반환한다() throws Exception {
        when(orderService.findByUserId("user-1")).thenReturn(List.of(sampleResult()));

        mockMvc.perform(get("/orders")
                        .param("userId", "user-1")
                        .header(AUTHENTICATED_USER_HEADER, "user-1"))
                .andExpect(status().isOk());
    }

    @Test
    void 타인의_주문_목록을_조회하면_403으로_거부하고_서비스를_호출하지_않는다() throws Exception {
        mockMvc.perform(get("/orders")
                        .param("userId", "user-1")
                        .header(AUTHENTICATED_USER_HEADER, "attacker"))
                .andExpect(status().isForbidden());

        verify(orderService, never()).findByUserId(any());
    }

    private static OrderResult sampleResult() {
        return new OrderResult("ORDER-1", "PRODUCT-001", 2, 1000, 2000, Instant.parse("2026-01-01T00:00:00Z"));
    }
}
