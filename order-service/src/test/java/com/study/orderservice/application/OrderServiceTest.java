package com.study.orderservice.application;

import com.study.orderservice.support.MySqlTestContainerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {"spring.config.import=", "spring.cloud.config.enabled=false"})
@ActiveProfiles("test")
@Import(MySqlTestContainerConfig.class)
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Test
    void 주문을_저장하면_주문번호로_다시_조회할_수_있다() {
        OrderResult placed = orderService.place(new PlaceOrderCommand("user-1", "PRODUCT-001", 3, 1500));

        Optional<OrderResult> found = orderService.findByOrderId(placed.orderId());

        assertThat(found).isPresent();
        assertThat(found.get().totalPrice()).isEqualTo(4500L);
    }

    @Test
    void 사용자별_주문만_조회된다() {
        orderService.place(new PlaceOrderCommand("user-2", "PRODUCT-002", 1, 1000));
        orderService.place(new PlaceOrderCommand("user-3", "PRODUCT-003", 1, 2000));

        List<OrderResult> orders = orderService.findByUserId("user-2");

        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).productId()).isEqualTo("PRODUCT-002");
    }

    @Test
    void 존재하지_않는_주문번호로_조회하면_빈_결과를_반환한다() {
        Optional<OrderResult> found = orderService.findByOrderId("NO-SUCH-ORDER");

        assertThat(found).isEmpty();
    }

    @Test
    void int_범위를_넘는_금액도_DB_에_그대로_저장되고_조회된다() {
        OrderResult placed = orderService.place(new PlaceOrderCommand("user-5", "PRODUCT-001", 2, 3_000_000_000L));

        Optional<OrderResult> found = orderService.findByOrderId(placed.orderId());

        assertThat(found).isPresent();
        assertThat(found.get().unitPrice()).isEqualTo(3_000_000_000L);
        assertThat(found.get().totalPrice()).isEqualTo(6_000_000_000L);
    }

    @Test
    void 저장된_주문에는_생성시각이_채워진다() {
        OrderResult placed = orderService.place(new PlaceOrderCommand("user-4", "PRODUCT-001", 2, 1500));

        assertThat(placed.createdAt()).isNotNull();
    }
}
